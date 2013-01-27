package ds.lab.log;

import java.io.IOException;

import ds.lab.message.TimeStampMessage;

public interface LoggerApi {
	/**
	 * Similar to send()
	 * @param message
	 * @throws IOException 
	 */
	void log(TimeStampMessage message) throws IOException;
	void log(LogLevel level, TimeStampMessage message) throws IOException;
}
