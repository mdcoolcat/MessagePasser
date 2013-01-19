package ds.lab.messagepasser;

import java.util.ArrayList;

import ds.lab.message.Message;

public interface MessagePasserApi {
	void send(Message message);
	ArrayList<Message> receive();
}
