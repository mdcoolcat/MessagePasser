package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import ds.lab.message.Message;

/**
 * thread from listener thread, responsible for reading from socket and add to
 * inputQueue. Be careful on synchronizing queue (should be fine since I use
 * blockingQueue).
 * <p>
 * 
 * need to close them
 * 
 * @author dmei
 * 
 */
public class WorkerThread implements Runnable {
	private ObjectInputStream in;
	private Socket connection;
	private BlockingQueue<Message> inputQueue; // input queue
	// private static HashMap<String, Socket> sockMap;
	private String remoteName;

	Object rcved = null;

	public WorkerThread(Socket connection, BlockingQueue<Message> inputQueue, String remoteName) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		this.remoteName = remoteName;
		new Thread(this).start();
	}

	// public static HashMap<String, Socket> getSockMap()
	// {
	// return sockMap;
	// }
	@Override
	public void run() {
		try {
			in = new ObjectInputStream(connection.getInputStream());
			while (true) {
				rcved = in.readObject();
				// TODO data type swtich
				Message tmp = (Message) rcved;
				if (tmp.getData() == null) {
					System.err.println("Messager> " + connection.getInetAddress().getHostAddress() + " went offile");
				} else {
					inputQueue.add(tmp);
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

}
