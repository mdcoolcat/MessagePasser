package ds.lab.messagepasser;

import java.util.ArrayList;

import ds.lab.message.TimeStampMessage;

public interface MessagePasserApi {
//	void send(Message message);
	ArrayList<TimeStampMessage> receive();
	void send(TimeStampMessage message);
}
