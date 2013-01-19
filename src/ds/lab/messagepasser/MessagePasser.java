package ds.lab.messagepasser;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import ds.lab.bean.NodeBean;
import ds.lab.bean.RuleBean;
import ds.lab.message.Message;
import ds.lab.message.MessageAction;
import ds.lab.message.MessageKind;

public class MessagePasser implements MessagePasserApi {
	private int port = 6781;
	/** multithreading, node management */
	private final int MAX_THREAD = Config.NUM_NODE;
	private ServerSocket listenSocket;
	private HashMap<String, NodeBean> nodeList;// name-user
	// private HashMap<String, Socket> sockMap;// name-socket
	/** message */
	private BlockingQueue<Message> inputQueue; // input queue
	private BlockingQueue<Message> outputQueue; // output queue
//	private Message incoming;
	/** other local information */
	private String localName;
//	private int lastId = -1;// TODO
	private int[] nthTracker;
	private final int NUM_ACTION = 3;//DROP, DUPLICATE, DELAY
	AtomicInteger lastId;

	/**
	 * Constructor read yaml formatted configure file, parse "configuration"
	 * section into nodeList as NodeBean;parse sendRule and rcvRule
	 * 
	 * @param configurationFile
	 * @param localName
	 *            global unique name to identify this node
	 * @throws IOException
	 */
	public MessagePasser(String configurationFile, String localName)
			throws IOException {
		inputQueue = new LinkedBlockingDeque<Message>();
		outputQueue = new LinkedBlockingDeque<Message>();
		nodeList = new HashMap<String, NodeBean>();
		nthTracker = new int[NUM_ACTION];
		// TODO maintain sockets for reuse
		// sockMap = new HashMap<String, Socket>();
		// TODO read file...nodelist..rules, code in Config.java, replace this by Config.NODELIST
		nodeList.put(localName, new NodeBean(localName, "192.168.145.1", port));
		nodeList.put("alice", new NodeBean("alice", "192.168.145.136", 1234));
		
		/* build my listening socket */
		NodeBean me = nodeList.get(localName);
		if (me == null) {
			throw new IllegalArgumentException("Error local name");
		}
		this.localName = localName;
//		this.incoming = new Message();// for initial use
		this.lastId = new AtomicInteger(-1);
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
		if (argv.length < 1) {
			System.out.println("Usage: ...");
			System.exit(0);
		}

		try {
			new MessagePasser("", argv[0].toLowerCase());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(Message message) {
		// TODO check rules, sync message id
//		ArrayList<RuleBean> rules = Config.SENDRULES;
//		boolean isDuplicate, isDelay, isDrop;
//		for (RuleBean r : rules) {
//			if (!r.isMatch(message)) {//TODO caution, not check yet
//				System.err.println("SendRule check fails: message dropped");
//				return;
//			}
////			isDuplicate = r.isDuplicate();
////			isDelay = r.isDelay();
////			isDrop = r.isDrop();
//		}
		message.setId(lastId.addAndGet(1));
		System.out.println(message);
		/* TODO action */
		
		outputQueue.add(message);
		try {
			while (!outputQueue.isEmpty())
				connectAndSend(outputQueue.remove());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void connectAndSend(Message message) throws UnknownHostException,
			IOException {
		String dest = message.getDest();
		// Socket sendSock = sockMap.get(dest);
		// if (sendSock == null || sendSock.isClosed()) {
		NodeBean n = nodeList.get(dest);
		if (n == null)
			throw new UnknownHostException("Message Send fails: unknown host " + dest);
		Socket sendSock = new Socket(n.getIp(), n.getPort());
		// synchronized (sockMap) {
		// sockMap.put(dest, sendSock);
		// }
		// }
		ObjectOutputStream out = new ObjectOutputStream(
				sendSock.getOutputStream());
		out.writeObject(message);
		out.flush();
		sendSock.close();//TODO remove if implement reuse
	}

	@Override
	public Message receive() {
		if (!inputQueue.isEmpty())
			return inputQueue.remove();
		return null;
	}

	private class ListenThread implements Runnable {
		@Override
		public void run() {
			System.err.println("Listener>>>>>I'm "+localName);
			/**
			 * when a new socket connected, create a new thread to handle the
			 * request, who's responsible for reading message from the socket
			 * and add to input queue
			 */
			try {
				while (true) {
					Socket connection = listenSocket.accept();
//					connection.setKeepAlive(true);
					System.err.println("Listener>>>>>Received: "
							+ connection.getInetAddress().toString());
					// TODO pass sockMap to the thread to add socket...
					new WorkerThread(connection, inputQueue);
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
			Scanner sc = new Scanner(System.in);
			while (true) {
				System.err.println("**********Choose: 0. Send(S)\t1. Receive(R)");
				String input = sc.nextLine().toLowerCase();
				//TODO string input
				if (input.equals("0") || input.equals("send") || input.equals("s")) {
					System.err.print("**********TO: ");
					for (String name : nodeList.keySet()) {
						if (name.equals(localName))//skip self
							continue;
						System.err.print(name + "\t");
					}
					String dest = sc.nextLine();
					System.err.println("\n**********Input Message in 144 chars:");
					String outMessage = sc.nextLine();
					// TODO kind
					sendMessage(dest, MessageKind.NONE, outMessage);

				} else if (input.equals("1") || input.equals("receive") || input.equals("r")) {
					Message incoming = receive();
					if (incoming != null) {
						lastId.addAndGet(1);
						System.out.println(incoming.getSrc() + ">"
								+ incoming.getData());
						System.err.println("msg left in queue: "+inputQueue.size());
					} else
						System.err.println("**********no message");
				} else {
					System.err.println("Invalid input");
				}
			}
		}

		private void sendMessage(String dest, MessageKind kind, String data) {
			send(new Message(localName, dest, kind, data));
		}
	}
}
