package ds.lab.messagepasser;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import ds.lab.bean.NodeBean;
import ds.lab.message.Message;

public class MessagePasser implements MessagePasserApi {
	private int port = 6781;
	/** multithreading, node management */
	private final int MAX_THREAD = 4;
	private ServerSocket listenSocket;
	private HashMap<String, NodeBean> nodeList;// name-user
	// private HashMap<String, Socket> sockMap;// name-socket
	/** message */
	private BlockingQueue<Message> inputQueue; // input queue
	private BlockingQueue<Message> outputQueue; // output queue
	private Message incoming;
	/** other local information */
	private String localName;
	private int lastId = -1;// TODO

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
		// TODO maintain sockets for reuse
		// sockMap = new HashMap<String, Socket>();
		// TODO read file...nodelist..rules
		nodeList.put(localName, new NodeBean(localName, "192.168.145.1", port));
		nodeList.put("alice", new NodeBean("alice", "192.168.145.135", 1234));
		/* build my listening socket */
		NodeBean me = nodeList.get(localName);
		if (me == null) {
			throw new IllegalArgumentException("Error local name");
		}
		this.localName = localName;
		this.incoming = new Message();// for initial use
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
			MessagePasser me = new MessagePasser("", argv[0].toLowerCase());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(Message message) {
		// TODO check rules, sync message id
		message.setId(++lastId);
		System.out.println(message);
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
		Socket sendSock = new Socket(n.getIp(), n.getPort());
		// synchronized (sockMap) {
		// sockMap.put(dest, sendSock);
		// }
		// }
		ObjectOutputStream out = new ObjectOutputStream(
				sendSock.getOutputStream());
		out.writeObject(message);
		out.flush();
	}

	@Override
	public Message receive() {
		System.out.println("in receive..." + lastId);
		if (!inputQueue.isEmpty())
			incoming = inputQueue.remove();
		System.out.println("message left in queue: " + inputQueue.size());
		return incoming;
	}

	private class ListenThread implements Runnable {
		// Socket connection = null;

		@Override
		public void run() {
			System.out.println(">>>>>I'm lisening...");
			/**
			 * when a new socket connected, create a new thread to handle the
			 * request, who's responsible for reading message from the socket
			 * and add to input queue
			 */
			try {
				while (true) {
					Socket connection = listenSocket.accept();
					connection.setKeepAlive(true);
					System.out.println(">>>>>Received: "
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
			System.out.println("**********User thread starts");
			Scanner sc = new Scanner(System.in);
			while (true) {
				System.out.println("**********Choose: 0. Send\t1. Receive");
				String input = sc.nextLine();
				int choose = Integer.parseInt(input);
				switch (choose) {
				case 0:
					System.out
							.println("**********To: 0. alice\t1. charlie\t...");
					String dest = sc.nextLine();
					System.out.println("**********Input Message in 144 chars:");
					String outMessage = sc.nextLine();
					// TODO kind
					sendMessage(dest, "default", outMessage);
					break;
				case 1:
					incoming = receive();
					if (incoming.getId() != lastId) {
						lastId = incoming.getId();
						System.out.println(incoming.getSrc() + ">"
								+ incoming.getData());
					} else
						System.err.println("**********no message");
					break;
				default:
					System.out.println("Invalid input");
				}
			}
		}

		private void sendMessage(String dest, String kind, String data) {
			send(new Message(localName, dest, kind, data));
		}
	}
}
