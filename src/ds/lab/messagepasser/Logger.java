package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import ds.lab.bean.NodeBean;
import ds.lab.message.TimeStampMessage;

/**
 * Similar to MessagePasser
 * 
 * @author dmei
 * 
 */
public class Logger {
	/** multithreading, node management */
	private int MAX_THREAD;
	private ServerSocket listenSocket;
	private HashMap<String, NodeBean> nodeList;// name-user
	/** message */
	private  ArrayList<TimeStampMessage> messageList;
	/** other local information */
	private final static String localName = "logger";
	private String configFileName;
	private HashMap<String, String> ipNameMap;	//for reverse lookup by ip, <ip, name>
	private Config config;
	/** clock */
	private ClockService clock;
	/** log file */
	private final String logPath = "log.txt";
	private FileWriter writer;

	public Logger(String configurationFile) throws IOException {
		config = new Config(configurationFile, localName);
		MAX_THREAD = config.NUM_NODE;
		//TODO create ClockService instance
		clock = ClockService.getClock(0, MAX_THREAD);
		nodeList =config.NODELIST;
		/* build my listening socket */
		NodeBean me = nodeList.get(localName);
		if (me == null) {
			throw new IllegalArgumentException("Config file error: no logger configuration");
		}
		this.configFileName=configurationFile;
		ipNameMap = new HashMap<String, String>();
		for (NodeBean n : nodeList.values())
			ipNameMap.put(n.getIp(), n.getName());
		/* messeage list */
		messageList = new ArrayList<TimeStampMessage>();//TODO comparator based on timestamps
		/* log file, shared by threads */
		writer = new FileWriter(logPath);
		/* listener */
		listenSocket = new ServerSocket(me.getPort());
		ListenThread listener = new ListenThread();
		// TODO for loop MAX_THREAD
		new Thread(listener).start();
		UserThread userIf = new UserThread();
		new Thread(userIf).start();
	}

	private class ListenThread implements Runnable {
		@Override
		public void run() {
			System.err.println(localName + " Starts");
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
					System.err.println("Listener> " + remote + " has connected you");
					new LoggerWorkerThread(connection, messageList, clock, writer);
				}
			} catch (EOFException e) {//someone offline
				String remote = e.getMessage();
				System.err.println("Messager> " + remote + " went offline");
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
				while (true) {
					System.err.println("Logger> Press any key to output logs");
					sc = new Scanner(System.in);
					sc.nextLine();
					System.out.println(messageList);//TODO sort?
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: configFile");
			System.exit(0);
		}

		try {
			new Logger(args[0]);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
