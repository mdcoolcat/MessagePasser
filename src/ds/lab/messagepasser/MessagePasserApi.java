package ds.lab.messagepasser;

import ds.lab.message.Message;

public interface MessagePasserApi {
	void send(Message message);
	Message receive();
}
