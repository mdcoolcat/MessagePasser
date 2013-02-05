package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ds.lab.bean.RuleBean;
import ds.lab.bean.TimeStamp;
import ds.lab.message.Message;
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
	private HashMap<Integer,HashMap<String,Boolean>> ackList;//track ack
	private final ArrayList<String> peers;//peer name
	private MessagePasser mp;

	public WorkerThread(MessagePasser mp, Socket connection, ArrayList<String> peers, BlockingQueue<TimeStampMessage> inputQueue,
			BlockingQueue<TimeStampMessage> delayInputQueue,  LinkedList<MulticastMessage> holdbackQueue, AtomicIntegerArray rcvNthTracker, ClockService clock,
			Config config, HashMap<Integer,HashMap<String,Boolean>> ackList) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		this.delayInputQueue = delayInputQueue;
		this.holdbackQueue = holdbackQueue;
		this.rcvNthTracker = rcvNthTracker;
		this.clock = clock;
		this.config = config;
		this.mp = mp;
		this.ackList = ackList;
		this.peers = peers;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(connection.getInputStream());
			while (true) {
				Message tmp = (Message) in.readObject();
				// if (tmp.getData() == null) {
				// System.err.println("Messager> " + tmp.getSrc() +
				// " went offline");
				// } else {
				assert tmp instanceof TimeStampMessage;
				TimeStampMessage message = (TimeStampMessage) tmp;
				this.localName = message.getDest();
				synchronized (clock) {
					// System.out.println(" my current ts: " +
					// clock.getCurrentTimeStamp(message.getDest()));
					TimeStamp ts = clock.updateTimeStampOnReceive(message.getDest(), message);
				}
				// System.out.println("after receive() my ts: " +
				// clock.getCurrentTimeStamp(message.getDest()));
				MessageAction action = checkReceiveRule(message);
				if (action == MessageAction.DROP || action == MessageAction.DELAY)
					continue;
				if (message instanceof MulticastMessage) {
					MulticastMessage multicast = (MulticastMessage) message;
					System.out.println("incoming<<< "+multicast);
					if (multicast.getType() == MulticastType.MESSAGE) {
						synchronized (this) {
							if (!holdbackQueue.contains(multicast)) {//new incoming multicast msg, begin track ack
								holdbackQueue.add(multicast);
								HashMap<String, Boolean> acks = new HashMap<String, Boolean>();
								for (String p : peers) {
									if (p.equals(multicast.getSrc()))
											continue;
									acks.put(p, false);
								}
								ackList.put(multicast.getMulticcastId(), acks);
								Thread.sleep(500);//TODO
							}
						}
						boolean isOrdered = checkTimeOrder(multicast);
						if (isOrdered)
							deliver(multicast, action);
					}

					checkAndAdd(multicast);
				} else {// deliver normal msg TODO or when multicast
					System.out.println("normal msg");
					deliver(message, action);
				}
			}
			// }

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
	 * @throws InterruptedException 
	 */
	synchronized private void checkAndAdd(MulticastMessage multicast) throws CloneNotSupportedException, InterruptedException {
		int theId = multicast.getMulticcastId();
		HashMap<String, Boolean> myAck = ackList.get(theId);
		System.out.println("checking --- current acklist: " + myAck);
		switch (multicast.getType()) {
		case MESSAGE:// TODO if have time, add duplicate
			// broadcast ack
			MulticastMessage toAck = new MulticastMessage(theId, multicast.getSrc(), localName, null, "ack",
					MulticastType.ACK, multicast.getData());
			for (String peer : myAck.keySet()) {
				MulticastMessage ack = toAck.clone();
				ack.setDest(peer);
				mp.send(ack);
			}
			break;
		case ACK://may be dropped by rule before enter this; or duplicate ack, fine
			String from = multicast.getForward();
			myAck.put(from, true);
			if (!holdbackQueue.contains(multicast)) {
				// i didn't get the message, send NACK TODO
				System.out.println("I didn't get the meesage. Send NACK");
				mp.send(new MulticastMessage(theId, multicast.getSrc(), localName, from, "nack",
						MulticastType.NACK, null));// src, myname, from..
			} else {
				boolean allRcv = true;
				for (boolean b : myAck.values()) {
					if (!b) {
						allRcv = false;
						break;
					}
				}
				if (allRcv) {// TODO and order preserved
					holdbackQueue.remove(multicast);
					System.out.println("all received");
					ackList.remove(theId);
				} 
			}
			break;
		case NACK:// this msg.forward didn't get message
			if (holdbackQueue.isEmpty()) {
				System.err.println("sequence wrong. they already ack, no need to forward.");
			} else {
				MulticastMessage toForward = null;
				ListIterator<MulticastMessage> it = holdbackQueue.listIterator();
				while (it.hasNext()) {
					MulticastMessage tmp = it.next();
					if (tmp.equals(multicast)) {
						toForward = tmp;
						break;
					}
				}
				if (toForward != null) {
					MulticastMessage theForward = toForward.clone();
					theForward.setForward(localName);
					theForward.setDest(multicast.getSrc());
					mp.send(theForward);// src, myname,
				} else {
					System.out.println("I didn't receive either...");
				}
			}
			break;
		}
		System.out.println("after checking --- current acklist: " + ackList.get(theId));

	}

	private boolean checkTimeOrder(MulticastMessage multicast) {
		// TODO Auto-generated method stub
		return true;
	}

	private void deliver(TimeStampMessage message, MessageAction action) throws CloneNotSupportedException {
		// TODO move from holdback queue
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
