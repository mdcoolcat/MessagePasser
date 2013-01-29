package ds.lab.log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import ds.lab.bean.NodeBean;
import ds.lab.message.TimeStampMessage;

/**
 * Logger of client side, responsible for keeping the connection to logger and send message to logger
 * @author dmei
 *
 */
public class LoggerFacility extends NodeBean implements LoggerApi {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1753302579872572685L;
	private ObjectOutputStream out;

	public LoggerFacility(String name, String ip, int port) {//logger's socket
		super(name, ip, port);
		Socket sendSocket;
		try {
			sendSocket = new Socket(ip, port);
			out = new ObjectOutputStream(sendSocket.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Meesager> Unknow host: logger IP incorrect");
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void log(TimeStampMessage message) {
		assert out != null;
		try {
			out.writeObject(message);
			out.flush();
			out.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void log(LogLevel level, TimeStampMessage message) {
		// TODO deal with level
		log(message);
	}
	
}
