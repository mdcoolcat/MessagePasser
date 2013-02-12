package ds.lab.clock;

import java.util.HashMap;

import ds.lab.bean.NodeBean;
import ds.lab.bean.TimeStamp;
import ds.lab.message.TimeStampMessage;

public abstract class ClockService {
	private static final int LOGICAL_ID = 0;
	private static final int VECTOR_ID = 1;
	
	private static ClockService clock = null;
	protected TimeStamp myTimeStamp;
	
	//TODO factory pattern ok???
	public static ClockService getClock(int clockId, String localName,int numOfNode,HashMap<String,NodeBean> nodelist) {
		switch (clockId) {
		case LOGICAL_ID:
			if (clock == null) 
				 clock = new LogicalClock(localName);
			break;
		case VECTOR_ID:
			if (clock == null)
				clock = new VectorClock(numOfNode,nodelist);
			break;
		default:
			clock = null;
		}
		return clock;
	}
	//TODO: required because its to be overridden differently by the 2 different clocks
	public abstract TimeStamp getNewTimeStamp(String localName);
	public abstract TimeStamp getCurrentTimeStamp(String localName);
	public abstract TimeStamp updateTimeStampOnReceive(String localName, TimeStampMessage Tsm);
}
