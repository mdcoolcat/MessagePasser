package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ds.lab.bean.TimeStamp;
import ds.lab.bean.VectorTimeStamp;
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
	private String remoteName;
	private ClockService clock;

	public WorkerThread(Socket connection, BlockingQueue<TimeStampMessage> inputQueue, ClockService clock,
			String remoteName) {
		super();
		this.connection = connection;
		this.inputQueue = inputQueue;// must lock
		this.clock = clock;
		this.remoteName = remoteName;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(connection.getInputStream());
			while (true) {
				// rcved = in.readObject();
				TimeStampMessage tmp = (TimeStampMessage) in.readObject();
				if (tmp.getData() == null) {
					System.err.println("Messager> " + connection.getInetAddress().getHostAddress() + " went offline");
				} else {
					inputQueue.add(tmp);
					synchronized (clock) {
						System.out.println(" my current ts: " + clock.getCurrentTimeStamp(tmp.getDest()));
						TimeStamp ts = clock.updateTimeStampOnReceive(tmp.getDest(), tmp);
					}
					System.out.println("after receive my ts: " + clock.getCurrentTimeStamp(tmp.getDest()));
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
