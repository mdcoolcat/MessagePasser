package ds.lab.messagepasser;

import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

import ds.lab.bean.TimeStamp;
import ds.lab.message.TimeStampMessage;

/**
 * Similar to WorkerThread
 * @author dmei
 *
 */
public class LoggerWorkerThread implements Runnable {
	private ObjectInputStream in;
	private Socket connection;
	private ArrayList<TimeStampMessage> messageList;
	private ClockService clock;
	private FileWriter writer;
	private final String format = "%s\t%s: %d - %s\n";//TODO level, datetime, messageId, message

	Object rcved = null;

	public LoggerWorkerThread(Socket connection, ArrayList<TimeStampMessage> messageList, ClockService clock, FileWriter writer) {
		super();
		this.connection = connection;
		this.messageList = messageList;
		this.clock = clock;
		this.writer = writer;// must lock. synchronize or readWriteLock...
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
					synchronized (this) {
						TimeStamp ts = clock.getTimeStamp();
						//TODO set timestamp
						messageList.add(tmp);
						writer.write(formatLog(tmp));
					}
					
				}
			}

		} catch (EOFException e) {
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String formatLog(TimeStampMessage msg) {
		return String.format(format, msg.getKind(), msg.getId(), msg.getData().toString());
	}

}
