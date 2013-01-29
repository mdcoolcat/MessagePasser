package ds.lab.messagepasser;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

import ds.lab.message.TimeStampMessage;

/**
 * Similar to WorkerThread
 * 
 * @author dmei
 * 
 */
public class LoggerWorkerThread implements Runnable {
	private ObjectInputStream in;
	private Socket connection;
	private ArrayList<TimeStampMessage> messageList;
//	private ClockService clock;
	private File file;
	private BufferedWriter writer;
	private final String format = "%s: %s - %s\n";// TODO timestamp, level, , message

	Object rcved = null;

	public LoggerWorkerThread(Socket connection, ArrayList<TimeStampMessage> messageList, File file) throws IOException {
		super();
		this.connection = connection;
		this.messageList = messageList;
//		this.clock = clock;
		this.file = file;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(connection.getInputStream());
			while (true) {
				rcved = in.readObject();
				// TODO data type swtich
				TimeStampMessage tmp = (TimeStampMessage) rcved;
				if (tmp.getData() == null) {
					System.err.println("Messager> " + connection.getInetAddress().getHostAddress() + " went offile");
				} else {
					synchronized (messageList) {
//						TimeStamp ts = clock.getTimeStamp();
						// TODO set timestamp
						messageList.add(tmp);
					}
					writer = new BufferedWriter(new FileWriter(file, true));
					synchronized (writer) {
						System.out.println("writer wait....");
//						writer.wait();
						writer.append(formatLog(tmp));
						System.out.println("write done...");
//						writer.notifyAll();
						writer.close();
					}
						
					}
						
			}

		} catch (EOFException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String formatLog(TimeStampMessage msg) {
		System.out.println(msg);
		return String.format(format, msg.getTimeStamp(), msg.getKind(), msg.getData().toString());
	}

}
