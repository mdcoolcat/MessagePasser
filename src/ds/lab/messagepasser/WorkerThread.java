package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ds.lab.bean.RuleBean;
import ds.lab.bean.TimeStamp;
import ds.lab.clock.ClockService;
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
	private BlockingQueue<MulticastMessage> delayHoldbackQueue;
	private BlockingQueue<MulticastMessage> requestQueue;
	private AtomicIntegerArray rcvNthTracker;
	private ClockService clock;
	private Config config;
	private String localName, remoteName;
	private HashMap<Integer, HashMap<String, Boolean>> ackList;// track ack
	private AtomicInteger lastMulticastId;
//	private final ArrayList<String> group;// peer name
	private final String[] group;
	// private final Lock lock;;
	private MessagePasser mp;

	public WorkerThread(MessagePasser mp, Socket connection) {
		super();
		this.mp = mp;
		this.connection = connection;
		this.inputQueue = mp.inputQueue;// must lock
		this.delayInputQueue = mp.delayInputQueue;
		this.holdbackQueue = mp.holdbackQueue;
		this.delayHoldbackQueue = mp.delayHoldbackQueue;
		this.rcvNthTracker = mp.rcvNthTracker;
		this.clock = mp.clock;
		this.config = mp.config;
		this.ackList = mp.ackList;
		this.lastMulticastId = mp.lastMulticastId;
		this.group = mp.group;
		// this.lock = mp.threadLock;
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
				this.remoteName = message.getSrc();
				synchronized (clock) {
					// System.out.println(" my current ts: " +
					// clock.getCurrentTimeStamp(message.getDest()));
					TimeStamp ts = clock.updateTimeStampOnReceive(message.getDest(), message);
				}
				// System.out.println("after receive() my ts: " +
				// clock.getCurrentTimeStamp(message.getDest()));
				MessageAction action = checkReceiveRule(message);
//				System.err.println("receive rule: " + action);
				if (action == MessageAction.DROP || action == MessageAction.DELAY)
					continue;
				if (message instanceof MulticastMessage) {
					MulticastMessage multicast = (MulticastMessage) message;
					System.out.println("incoming<<< " + multicast);
					// if (lock.tryLock()) {
					// try {
					// manipulate protected state
					checkAndAdd(multicast, action);
					// } finally {
					// lock.unlock();
					// }
					// }
				} else {// deliver normal msg TODO or when multicast
					System.out.println("normal msg");
					deliver(message, action);
				}
			}
			// }

		} catch (EOFException e) {
			try {
				in.close();
				throw new EOFException(remoteName);
			} catch (IOException e1) {
				// e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param src
	 *            original source
	 * @return
	 */
	private HashMap<String, Boolean> getAckTracker(String src) {
		HashMap<String, Boolean> acks = new HashMap<String, Boolean>();
		for (String p : group) {
			if (p.equals(src))
				continue;
			acks.put(p, false);
		}
		return acks;
	}

	/**
	 * check duplidate, timestamps, update ackMap
	 * 
	 * @param multicast
	 * @param action
	 * @throws CloneNotSupportedException
	 * @throws InterruptedException
	 */
	private void checkAndAdd(MulticastMessage multicast, MessageAction action) throws CloneNotSupportedException, InterruptedException {
		int theId = multicast.getMulticcastId();
		String from = multicast.getSrc();
		String origin = multicast.getOrigin();
		synchronized (ackList) {
			if (!ackList.containsKey(theId))
				ackList.put(multicast.getMulticcastId(), getAckTracker(multicast.getOrigin()));
		}
		switch (multicast.getType()) {
		case REQUEST:
			LockState lockState;
			boolean voted;
			synchronized (mp) {
				lockState = mp.state;
				voted = mp.voted;
			}
			if (lockState == LockState.HELD || voted) {
				System.out.println("SOMEONE IS HOLDING THE LOCK. DO NOT VOTE. QUEUE THE REQUEST.");
				synchronized (holdbackQueue) {
					holdbackQueue.add(multicast);
				}
			} else {
				System.out.println("vote for "+from);
				synchronized (mp) {//unicast
					mp.send(new MulticastMessage(theId, origin, localName, from, "vote", MulticastType.VOTE, null));
					mp.voted = true;
				}
			}
			break;
		case MESSAGE:
			synchronized (holdbackQueue) {
				if (!holdbackQueue.contains(multicast)) {
					holdbackQueue.add(multicast);
					// Thread.sleep(500);// TODO
					if (checkTimeOrder(multicast))
						deliver(multicast, action);
					else {
						System.out.println("unordered...");
						if (!delayHoldbackQueue.isEmpty()) {
							while (!delayHoldbackQueue.isEmpty())
								// popup all delayed
								holdbackQueue.add(delayHoldbackQueue.remove());
							delieverAll(action);
						}
					}
				}
			}

			// broadcast ack
			MulticastMessage toAck = new MulticastMessage(theId, origin, localName, null, "ack", MulticastType.ACK, multicast.getData());
			for (String peer : group) {
				if (peer.equalsIgnoreCase(origin))
					continue;
				MulticastMessage ack = toAck.clone();
				ack.setDest(peer);
				mp.send(ack);
			}
			break;
		case RELEASE:
			if (holdbackQueue.isEmpty()) {
				synchronized (mp) {
					mp.voted = false;
				}
			} else {
				MulticastMessage next;
				synchronized (holdbackQueue) {
					next = holdbackQueue.remove();
				}
				String requester = next.getOrigin();
				System.out.println("vote for "+ requester);
				synchronized (mp) {//unicast
					mp.send(new MulticastMessage(next.getMulticcastId(), requester, localName, requester, "vote", MulticastType.VOTE, null));
					mp.voted = true;
				}
			}
			break;
		case VOTE:
			synchronized (ackList) {
				ackList.get(theId).put(from, true);
			}
			break;
		case ACK:// may be dropped by rule before enter this; or duplicate ack,
					// fine
			synchronized (ackList) {
				ackList.get(theId).put(from, true);
				if (!holdbackQueue.contains(multicast) && !checkAllRcv(theId)) {
					// i didn't get the message, send NACK
					System.out.println("I didn't get the meesage. Send NACK");
					mp.send(new MulticastMessage(theId, origin, localName, from, "nack", MulticastType.NACK, null));// src,
				} else {
//					System.out.println("sequence wrong");
				}
			}

			break;
		case NACK:// this msg.forward didn't get message
			synchronized (holdbackQueue) {
				if (holdbackQueue.isEmpty()) {
//					System.err.println("sequence wrong. they already ack, no need to forward.");
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
						assert theForward.getOrigin().equals(origin);
						theForward.setSrc(localName);// origin->localname->from
						theForward.setDest(from);
						mp.send(theForward);// src, myname,
					} else {
						System.out.println("I didn't receive either...");
					}
				}
			}
			break;
		}
//		if (holdbackQueue.contains(multicast)) {// check if all receive
			// only when itself has
			// the msg
			if (checkAllRcv(theId)) {// TODO and order preserved
				holdbackQueue.remove(multicast);
				synchronized (mp.state) {
					mp.state = LockState.HELD;
				}
				System.out.println("\nGOT LOCK@@@@@@@@ "+localName+" @@@@@@@@LOCK GOT\n");
				// ackList.remove(theId);
			}
//		}
//		System.out.println("after checking --- current acklist: " + ackList.get(theId));

	}

	private void delieverAll(MessageAction action) throws CloneNotSupportedException {
		Collections.sort(holdbackQueue, new Comparator<MulticastMessage>() {
			@Override
			public int compare(MulticastMessage o1, MulticastMessage o2) {
				return o1.getMulticcastId() - o2.getMulticcastId();
			}
		});
		MulticastMessage last = holdbackQueue.getLast();
		for (MulticastMessage m : holdbackQueue) {
			if (m == last)
				deliver(m, action);
			else
				deliver(m, MessageAction.DEFAULT);
		}
		lastMulticastId.set(last.getMulticcastId());
	}

	private boolean checkAllRcv(int theId) {
		boolean allRcv = true;
		synchronized (ackList) {
			for (boolean b : ackList.get(theId).values()) {
				if (!b) {
					allRcv = false;
					break;
				}
			}
		}
		return allRcv;
	}

	// cannot create scenario to mess up them
	private boolean checkTimeOrder(MulticastMessage multicast) {
		int last = lastMulticastId.get();
		int current = multicast.getMulticcastId();
		if (last < 0 || (current - last) <= 1) {
			lastMulticastId.set(current);
			return true;
		}
		return false;
	}

	private void deliver(TimeStampMessage message, MessageAction action) throws CloneNotSupportedException {
		inputQueue.add(message);
//		if (action == MessageAction.DUPLICATE) {
//			rcvNthTracker.incrementAndGet(1);
//			TimeStampMessage dup = message.clone();// add to list later
//			dup.setId(message.getId());
//			dup.setTimeStamp(message.getTimeStamp());
//			inputQueue.add(dup);
//		}
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
		if (action == MessageAction.DELAY) {
			if (message instanceof MulticastMessage)
				delayHoldbackQueue.add((MulticastMessage) message);
			else
				delayInputQueue.add(message);
		}
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
		int now = rcvNthTracker.incrementAndGet(r.getActionIndex());
		if ((now == r.getNth()) || (r.getEveryNth() > 0 && (now % r.getEveryNth()) == 0))
			return r.getAction();
		return MessageAction.DEFAULT;
	}

}