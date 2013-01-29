package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ds.lab.bean.NodeBean;
import ds.lab.bean.RuleBean;
import ds.lab.bean.TimeStamp;
import ds.lab.log.LogLevel;
import ds.lab.log.LoggerFacility;
import ds.lab.message.MessageAction;
import ds.lab.message.TimeStampMessage;

public class MessagePasser implements MessagePasserApi {
	/** multithreading, node management */
	private int MAX_THREAD;
	private ServerSocket listenSocket;
	private HashMap<String, NodeBean> nodeList;// name-user
	private HashMap<String, ObjectOutputStream> outStreamMap;// name-socket
	/** message */
	private BlockingQueue<TimeStampMessage> inputQueue; // input queue
	private BlockingQueue<TimeStampMessage> outputQueue; // output queue
	private BlockingQueue<TimeStampMessage> delayInputQueue; // input queue
	private BlockingQueue<TimeStampMessage> delayOutputQueue; // output queue
	/** other local information */
	private String localName;
	private String configFileName;
	private HashMap<String, String> ipNameMap;	//for reverse lookup by ip, <ip, name>
	private AtomicIntegerArray sendNthTracker;
	private AtomicIntegerArray rcvNthTracker;
	private AtomicInteger lastId;
	private final int NUM_ACTION = 3;// DROP, DUPLICATE, DELAY
	private Config config;

	/** clock and logger */
	private ClockService clock;
	private LoggerFacility logger;

	/**
	 * Constructor read yaml formatted configure file, parse "configuration"
	 * section into nodeList as NodeBean;parse sendRule and rcvRule
	 * 
	 * @param configurationFile
	 * @param localName
	 *            global unique name to identify this node
	 * @throws IOException
	 */
	public MessagePasser(String configurationFile, String localName) throws IOException {
		config = new Config(configurationFile, localName);
		MAX_THREAD = config.NUM_NODE;
		//TODO create ClockService instance
		nodeList =config.NODELIST;
		/* logger */
		NodeBean lg = nodeList.get("logger");
		if (lg == null) {
			throw new IllegalArgumentException("Logger not found");
		}
		logger = new LoggerFacility(lg.getName(), lg.getIp(), lg.getPort());
		nodeList.remove("logger");
		Scanner sc = new Scanner(System.in);
		System.err.println("Enter type of clock that you require for your application");
		System.out.println("0. Logical \t 1. Vector");
		String input=sc.nextLine().toLowerCase();
		int numOfNodes;
		int clockid=0;
		if(input.equals("0")||input.equals("logical")||input.equals("l"))
		{
		  	clockid=0;
		  	numOfNodes=0;
		  	clock = ClockService.getClock(clockid,localName,numOfNodes,nodeList);
		}
		else if(input.equals("1")||input.equals("vector")||input.equals("v"))
		{
		  	clockid=1;
		  	numOfNodes=MAX_THREAD;
		  	clock = ClockService.getClock(clockid,localName,numOfNodes,nodeList);
		}
		System.out.println("Clock selected:"+clockid);
	//	System.out.println(clock.getCurrentTimeStamp(localName).getVector().get(localName).get());
		//TODO create logger
		
		/* build my listening socket */
		NodeBean me = nodeList.get(localName);
		if (me == null) {
			throw new IllegalArgumentException("Error local name");
		}
		this.localName = localName;
		this.configFileName=configurationFile;
		lastId = new AtomicInteger(-1);
		ipNameMap = new HashMap<String, String>();
		for (NodeBean n : nodeList.values())
			ipNameMap.put(n.getIp(), n.getName());

		/* queues and trackers */
		inputQueue = new LinkedBlockingDeque<TimeStampMessage>();
		outputQueue = new LinkedBlockingDeque<TimeStampMessage>();
		delayInputQueue = new LinkedBlockingDeque<TimeStampMessage>();
		delayOutputQueue = new LinkedBlockingDeque<TimeStampMessage>();
		sendNthTracker = new AtomicIntegerArray(NUM_ACTION);
		rcvNthTracker = new AtomicIntegerArray(NUM_ACTION);
		/* listener */
		outStreamMap = new HashMap<String, ObjectOutputStream>();
		listenSocket = new ServerSocket(me.getPort());
		ListenThread listener = new ListenThread();
		// TODO for loop MAX_THREAD
		new Thread(listener).start();
		UserThread userIf = new UserThread();
		new Thread(userIf).start();
	}

	/**
	 * 
	 * @param argv
	 *            local name of this node
	 */
	public static void main(String argv[]) {
		
		if (argv.length < 2) {
			System.out.println("Usage: configFile localName");
			System.exit(0);
		}

		try {
			new MessagePasser(argv[0], argv[1].toLowerCase());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void send(TimeStampMessage message) {
		// TODO sync message id
		message.setId(lastId.incrementAndGet());
		//TODO: We should convert the message to timestampmessage type here
		//because even the messaages of kind drop should have a timestamp.
		
		//TimeStampMessage tsm=new TimeStampMessage(message.getSrc(),message.getDest(),message.getKind(),message.getData());
		
		TimeStamp ts=this.clock.getNewTimeStamp(message.getSrc());
		message.setTimeStamp(ts);
		System.out.println("prepare to send: "+message.getTimeStamp());
		RuleBean theRule = getMatchedSendRule(message);
		MessageAction action;
		if (theRule == null) {
			action = MessageAction.DEFAULT;
		} else {
			action = theRule.getAction();
			if (action != MessageAction.DEFAULT)
				action = checkSendAction(theRule);
		}
		System.out.println(action);
		try {
			if (action != MessageAction.DEFAULT)
				logger.log(message);
			TimeStampMessage dup = null;
			switch (action) {
			case DROP:// ignore message
				message = null;
				break;
			case DELAY:
				
				delayOutputQueue.add(message);// don't send
				break;
			case DUPLICATE:
				sendNthTracker.incrementAndGet(1);
				dup = message.clone();
				dup.setId(message.getId());
				
			case DEFAULT:
				outputQueue.add(message);
				// now there should be two identical messages. Send delayed after normal
				/*
				 * The dup should be added to outputqueue by
				 * outputqueue.add(dup) isn't it ?? ----this needs count for sending the 2 message. if another thread adds one msg between them, you don't know which two should send...my opinion
				 * RESOLVED-Added to the outputqueue
				 */
				connectAndSend(outputQueue.remove());//send the original message
				
				  if (dup != null)
					connectAndSend(dup);
				
				synchronized (delayOutputQueue) {
					while (!delayOutputQueue.isEmpty())
						connectAndSend(delayOutputQueue.remove());
				}
				
			}
		} catch (UnknownHostException e) {
			System.err.println("Messager> " + e.getMessage());
		} catch (SocketException e) {
			String msg = "Message cannot be delivered: " + message.getDest() + " is offline";
			System.err.println(msg);
			sendToLogger(LogLevel.WARNING, msg);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private RuleBean getMatchedSendRule(TimeStampMessage tsm) {
		ArrayList<RuleBean> rules = config.getSendRules();
		RuleBean theRule = null;
		for (RuleBean r : rules) {
			if (r.isMatch(tsm)) {
				theRule = r;
				break;
			}
		}
		return theRule;
	}

	private MessageAction checkSendAction(RuleBean r) {
		if (r.hasNoRestriction())// if no specify nth or everyNth, ignore action
									// field..
			return r.getAction();
		int now = sendNthTracker.incrementAndGet(r.getActionIndex());// counter++
		if ((now == r.getNth()) || (r.getEveryNth() > 0 && (now % r.getEveryNth()) == 0))
			return r.getAction();
		return MessageAction.DEFAULT;
	}
	private void connectAndSend(TimeStampMessage tsm) throws UnknownHostException, SocketException, IOException {
		String dest = tsm.getDest();
		//TODO setTimeStamp, or put it after building socket
		//TimeStamp has to be appended before checking rules (seeAlso send(Message msg))
		//DEBUGGING:
//		System.out.println(tsm.getSrc()+"\n"+tsm.getDest()+"\n"+tsm.getId()+"\n"+tsm.getKind()+"\n"+tsm.getData()+"\n"+tsm.getTimeStamp().getVector().get(tsm.getSrc()).get());
		
		ObjectOutputStream out = outStreamMap.get(dest);
		if (out == null) {// no socket connection yet, create it TODO if port change...
			NodeBean n = nodeList.get(dest);
			if (n == null)
				throw new UnknownHostException("Message Send fails: unknown host " + dest);
			Socket sendSocket = new Socket(n.getIp(), n.getPort());
			out = new ObjectOutputStream(sendSocket.getOutputStream());
			outStreamMap.put(dest, out);
		}
		out.writeObject(tsm);
		out.flush();
		out.reset();
		System.err.println("sent>>>>>>>>>msg"+tsm.getId() + " " + tsm.getTimeStamp());
	}
	@Override
	public ArrayList<TimeStampMessage> receive() {
		ArrayList<TimeStampMessage> incoming = null;
		if (!inputQueue.isEmpty()) {
			TimeStampMessage message = inputQueue.remove();// examine the 1st one
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
			System.out.println(action);
			try {
				if (action != MessageAction.DEFAULT)
					logger.log(message);
				TimeStampMessage dup = null;
				switch (action) {
				case DROP:// drop message TODO lastId???
					break;
				case DELAY:// move the message to delayQUeue
					delayInputQueue.add(message);
					break;
				case DUPLICATE:
					rcvNthTracker.incrementAndGet(1);
					dup = message.clone();// add to list later
					dup.setId(message.getId());
					// inputQueue.add(dup);
				case DEFAULT:
					// now there should be two identical messages to deliver.
					// deliver delayed messages after the new one(s)
					incoming = new ArrayList<TimeStampMessage>();
					incoming.add(message);
					if (dup != null)
						incoming.add(dup);
					synchronized (delayInputQueue) {
						while (!delayInputQueue.isEmpty())
							incoming.add(delayInputQueue.remove());
					}
				}

			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return incoming;
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

	
	
	private String getLocalName()
	{
		return localName;
	}
	private String getConfigFileName()
	{
		return configFileName;
	}

	private void sendToLogger(LogLevel level, String msg) {
		TimeStampMessage tsMesseage = new TimeStampMessage(localName, "logger", level.toString(), msg);
		tsMesseage.setTimeStamp(clock.getNewTimeStamp(localName));	//not sure should use this clock method or other ones
		logger.log(tsMesseage);
	}

	private class ListenThread implements Runnable {
		@Override
		public void run() {
			System.err.println("Listener> I'm " + localName);
			/**
			 * when a new socket connected, create a new thread to handle the
			 * request, who's responsible for reading message from the socket
			 * and add to input queue
			 */
			try {
				while (true) {
					Socket connection = listenSocket.accept();
//					 connection.setKeepAlive(true);
					assert connection.isConnected();
					String remote = connection.getInetAddress().getHostAddress();
					String msg = remote + " has connected to " + localName;
					System.err.println("Listener> " + remote + " has connected you");
					sendToLogger(LogLevel.INFO, msg);
					new WorkerThread(connection, inputQueue, clock, ipNameMap.get(remote));
				}
			} catch (EOFException e) {//someone offline
				String remote = e.getMessage();
				System.err.println("Messager> " + remote + " went offline");
				try {
					outStreamMap.get(remote).close();
					outStreamMap.remove(remote);
				} catch (IOException e1) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Responsible for interating with user: send or receive
	 *
	 * @author dmei
	 * 
	 */
	private class UserThread implements Runnable {
		@Override
		public void run() {
			Scanner sc = null;
			try {
				while (true) {
					System.err.println("Messager> Choose: 0. Send(S)\t1. Receive(R)");
					sc = new Scanner(System.in);
					String input = sc.nextLine().toLowerCase();
					if (input.equals("0") || input.equals("send") || input.equals("s")) {
						//callToParseAgain(getLocalName(),getConfigFileName());   ....this is already called in the 
						                         //getsendrules and getreceiverules functions, so no need to call again from here
						System.err.print("Messager> TO: ");
						for (String name : nodeList.keySet()) {
							if (name.equals(localName))// skip self
								continue;
							System.err.print(name + "\t");
						}
						String dest = sc.nextLine().toLowerCase();
						System.out.println("\nMessager> Enter kind of the message:");
						String mk = sc.nextLine();
						System.err.println("\nMessager> Input Message in 144 chars:");
						String outMessage = sc.nextLine();
						TimeStampMessage sentMsg = sendMessage(dest, mk, outMessage);//TODO may block?
						System.out.println("\nMessager> Do you want to log this message? Y/N");
						String ifLog = sc.nextLine();
						if (ifLog.equalsIgnoreCase("Y"))
							logger.log(sentMsg);

					} else if (input.equals("1") || input.equals("receive") || input.equals("r")) {
						ArrayList<TimeStampMessage> incoming = receive();
						if (incoming != null) {
							for (TimeStampMessage m : incoming) {
								synchronized (lastId) {
									if ((lastId.get() <  m.getId())||(lastId.get() ==  m.getId()))
										lastId.set(m.getId());
								}
								System.out.println(m.getSrc() + "> msg" + m.getId()+" "+m.getData());
							}
						} else
							System.err.println("Messager> no message");
					} else {
						System.err.println("Messager> Invalid input");
					}
				}
			} catch (NoSuchElementException e) {
				String msg = "Messager> User press CTRL+C. Bye!";
				System.err.println(msg);
				sendToLogger(LogLevel.WARNING, msg);
			} finally {
				sc.close();
			}
		}

		private TimeStampMessage sendMessage(String dest, String kind, Object data) {
			TimeStampMessage msg = new TimeStampMessage(localName, dest, kind.toLowerCase(), data);
			send(msg);
			return msg;
		}
	}
}
