package ds.lab.messagepasser;

import java.util.ArrayList;

import ds.lab.message.Message;
import ds.lab.message.TimeStampMessage;

public interface MessagePasserApi {
	void send(Message message);
	ArrayList<Message> receive();
}
