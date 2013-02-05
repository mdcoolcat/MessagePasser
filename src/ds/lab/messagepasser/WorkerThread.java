package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ds.lab.bean.RuleBean;
import ds.lab.bean.TimeStamp;
import ds.lab.bean.VectorTimeStamp;
import ds.lab.message.MessageAction;
import ds.lab.message.MulticastMessage;
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
	private BlockingQueue<MulticastMessage> holdbackQueue;
	private AtomicIntegerArray rcvNthTracker;
	private ClockService clock;
	private Config config;
	private String remoteName;

	public WorkerThread(Socket connection, BlockingQueue<TimeStampMessage> inputQueue, BlockingQueue<TimeStampMessage> delayInputQueue, AtomicIntegerArray rcvNthTracker, ClockService clock, Config config) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		this.delayInputQueue = delayInputQueue;
		this.holdbackQueue = new LinkedBlockingDeque<MulticastMessage>();
		this.rcvNthTracker = rcvNthTracker;
		this.clock = clock;
		this.config = config;
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
					this.remoteName = message.getDest();
					synchronized (clock) {
						System.out.println(" my current ts: " + clock.getCurrentTimeStamp(message.getDest()));
						TimeStamp ts = clock.updateTimeStampOnReceive(message.getDest(), message);
					}
					System.out.println("after receive my ts: " + clock.getCurrentTimeStamp(message.getDest()));
					MessageAction action = checkReceiveRule(message);
					if (action == MessageAction.DROP || action == MessageAction.DELAY)
						continue;
					if (message instanceof MulticastMessage) {
						//TODO
					} else {//deliver normal msg
						inputQueue.add(message);
						if (action == MessageAction.DUPLICATE) {
							rcvNthTracker.incrementAndGet(1);
							TimeStampMessage dup = message.clone();// add to list later
							dup.setId(message.getId());
							dup.setTimeStamp(message.getTimeStamp());
							inputQueue.add(dup);
						}
					}
				}
			}

		} catch (EOFException e) {
			try {
				in.close();
				throw new EOFException(remoteName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private MessageAction checkReceiveRule(TimeStampMessage message) {
		RuleBean theRule = getMatchedReceiveRule(message);
		System.err.println(theRule);
		MessageAction action;
		if (theRule == null) {
			action = MessageAction.DEFAULT;
		} else {
			action = theRule.getAction();
			if (action != MessageAction.DEFAULT)
				action = checkReceiveAction(theRule);
		}
		System.out.println("in worker thread: "+ action);
		if (action == MessageAction.DELAY)
			delayInputQueue.add(message);//TODO put into holdbackQueue
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
