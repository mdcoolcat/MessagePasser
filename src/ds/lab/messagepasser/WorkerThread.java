package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import ds.lab.message.Message;

/**
 * thread from listener thread, responsible for reading from socket and add to
 * inputQueue. Be careful on synchronizing queue (should be fine since I use
 * blockingQueue).
 * <p>
 * TODO may add socket to sockMap for reuse. If we want to reuse sockets, no
 * need to close them
 * 
 * @author dmei
 * 
 */
public class WorkerThread implements Runnable {
	private ObjectInputStream in;
	private Socket connection;
	private BlockingQueue<Message> inputQueue; // input queue
	// private HashMap<String, Socket> sockMap;

	Object rcved = null;

	public WorkerThread(Socket connection, BlockingQueue<Message> inputQueue) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		// this.sockMap = sockMap;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			// input, output
			in = new ObjectInputStream(connection.getInputStream());
			rcved = in.readObject();
			// TODO data type swtich
			Message tmp = (Message) rcved;
			System.out.println("Before receive: queue size "
					+ inputQueue.size());
			// synchronized (sockMap) {
			// sockMap.put(tmp.getSrc(), connection);
			// System.out.println("put sockmap: "+tmp.getSrc());
			// }
			if (tmp.getData() == null) {
				System.out.println(connection.getInetAddress().getHostAddress()
						+ " went offile");
			} else {
				inputQueue.add(tmp);
			}
			System.out
					.println("After receive: queue size " + inputQueue.size());

		} catch (IOException e) {
			if (e instanceof EOFException) {
				System.out.println("end...eof");
			} else {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			// TODO remove this block if reuse socket
			try {
				in.close();
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
