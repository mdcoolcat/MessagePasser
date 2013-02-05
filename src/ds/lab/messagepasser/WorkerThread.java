package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ds.lab.bean.RuleBean;
import ds.lab.bean.TimeStamp;
import ds.lab.message.MessageAction;
import ds.lab.message.MulticastMessage;
import ds.lab.message.MulticastType;
import ds.lab.message.TimeStampMessage;

/**
 * thread from listener thread, responsible for reading from socket and add to
 * inputQueue. Be careful on synchronizing queue (should be fine since I use
 * blockingQueue).
 * 
 * @author dmei
 * 
 */
public class WorkerThread implements Runnable {
	private ObjectInputStream in;
	private Socket connection;
	private BlockingQueue<TimeStampMessage> inputQueue; // input queue
	private BlockingQueue<TimeStampMessage> delayInputQueue;
	private LinkedList<MulticastMessage> holdbackQueue;
	private AtomicIntegerArray rcvNthTracker;
	private ClockService clock;
	private Config config;
	private String localName;
	private Hashtable<String, Boolean> ackMap;
	private MessagePasser mp;

	public WorkerThread(MessagePasser mp, Socket connection, BlockingQueue<TimeStampMessage> inputQueue,
			BlockingQueue<TimeStampMessage> delayInputQueue, AtomicIntegerArray rcvNthTracker, ClockService clock,
			Config config, ArrayList<String> peerNames) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		this.delayInputQueue = delayInputQueue;
		this.holdbackQueue = new LinkedList<MulticastMessage>();
		this.rcvNthTracker = rcvNthTracker;
		this.clock = clock;
		this.config = config;
		this.mp = mp;
		this.ackMap = new Hashtable<String, Boolean>();
		for (String name : peerNames)
			this.ackMap.put(name, false);
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(connection.getInputStream());
			while (true) {
				// rcved = in.readObject();
				TimeStampMessage message = (TimeStampMessage) in.readObject();
				if (message.getData() == null) {
					System.err.println("Messager> " + connection.getInetAddress().getHostAddress() + " went offline");
				} else {
					this.localName = message.getDest();
					synchronized (clock) {
//						System.out.println(" my current ts: " + clock.getCurrentTimeStamp(message.getDest()));
						TimeStamp ts = clock.updateTimeStampOnReceive(message.getDest(), message);
					}
					System.out.println("after receive() my ts: " + clock.getCurrentTimeStamp(message.getDest()));
					MessageAction action = checkReceiveRule(message);
					if (action == MessageAction.DROP || action == MessageAction.DELAY)
						continue;
					if (message instanceof MulticastMessage) {
						MulticastMessage multicast = (MulticastMessage) message;
						checkAndAdd(multicast);

					} else {// deliver normal msg TODO or when multicast
							// indicate can deliver..
						deliver(message, action);
					}
				}
			}

		} catch (EOFException e) {
			try {
				in.close();
				throw new EOFException(localName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * check duplidate, timestamps, update ackMap
	 * 
	 * @param multicast
	 * @throws CloneNotSupportedException
	 */
	private void checkAndAdd(MulticastMessage multicast) throws CloneNotSupportedException {
		System.out.println(multicast);
		switch (multicast.getType()) {
		case MESSAGE:// TODO should be new message...if have time, add duplicate
						// detect
			holdbackQueue.add(multicast);
			// TODO when to deliver
			//broadcast ack TODO perhaps should clone..
			multicast.setType(MulticastType.ACK);
			multicast.setForward(localName);
			System.out.println("broadcast ack...");
			for (String peer : ackMap.keySet()) {
				multicast.setDest(peer);
				mp.send(multicast);
			}
			break;
		case ACK:
			ackMap.put(multicast.getForward(), true);
			if (!holdbackQueue.contains(multicast)) {
				// i didn't get the message, send NACK TODO
				System.out.println("I didn't get the meesage. Send NACK");
				mp.send(new MulticastMessage(multicast.getSrc(), localName, multicast.getForward(), null,
						MulticastType.NACK, null));//src, myname, from..
			}
			break;
		case NACK://this msg.forward didn't get message
			assert !holdbackQueue.isEmpty();
			MulticastMessage toForward = null;
			ListIterator<MulticastMessage> it = holdbackQueue.listIterator();
			while (it.hasNext()) {
				MulticastMessage tmp = it.next();
				if (tmp.equals(multicast)) {
					toForward = tmp;
					break;
				}
			}
//			assert toForward != null;
//			MulticastMessage theForward = new MulticastMessage(toForward.getSrc(), localName, toForward.getForward(), toForward.getKind(),
//					MulticastType.MESSAGE, toForward.getData());
//			theForward.setId(toForward.getId());//change ID???
//			theForward.setMulticcastId(toForward.getMulticcastId());
			toForward.setForward(localName);
			mp.send(toForward);//src, myname, from..TODO clone?
			break;
		}

	}

	private void deliver(TimeStampMessage message, MessageAction action) throws CloneNotSupportedException {
		//TODO move from holdback queue
		inputQueue.add(message);
		if (action == MessageAction.DUPLICATE) {
			rcvNthTracker.incrementAndGet(1);
			TimeStampMessage dup = message.clone();// add to list later
			dup.setId(message.getId());
			dup.setTimeStamp(message.getTimeStamp());
			inputQueue.add(dup);
		}
	}

	private MessageAction checkReceiveRule(TimeStampMessage message) {
		RuleBean theRule = getMatchedReceiveRule(message);
		MessageAction action;
		if (theRule == null) {
			action = MessageAction.DEFAULT;
		} else {
			action = theRule.getAction();
			if (action != MessageAction.DEFAULT)
				action = checkReceiveAction(theRule);
		}
		if (action == MessageAction.DELAY)
			delayInputQueue.add(message);// TODO put into holdbackQueue
		return action;
	}

	private RuleBean getMatchedReceiveRule(TimeStampMessage tsm) {
		ArrayList<RuleBean> rules = config.getRcvRules();
		RuleBean theRule = null;
		for (RuleBean r : rules) {
			if (r.isMatch(tsm)) {
				theRule = r;
				break;
			}
		}
		return theRule;
	}

	private MessageAction checkReceiveAction(RuleBean r) {
		if (r.hasNoRestriction())// if no specify nth or everyNth, ignore action
									// field..
			return r.getAction();
		int now = rcvNthTracker.incrementAndGet(r.getActionIndex());// TODO
																	// caution
		if ((now == r.getNth()) || (r.getEveryNth() > 0 && (now % r.getEveryNth()) == 0))
			return r.getAction();
		return MessageAction.DEFAULT;
	}

}
