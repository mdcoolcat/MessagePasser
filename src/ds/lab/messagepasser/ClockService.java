package ds.lab.messagepasser;

import ds.lab.bean.TimeStamp;

public abstract class ClockService {
	private static final int LOGICAL_ID = 0;
	private static final int VECTOR_ID = 1;
	
	private static ClockService clock = null;
	protected TimeStamp myTimeStamp;
	
	//TODO factory pattern ok???
	public static ClockService getClock(int clockId, int numOfNode) {
		switch (clockId) {
		case LOGICAL_ID:
			if (clock == null) 
				 clock = new LogicalClock();
			break;
		case VECTOR_ID:
			if (clock == null)
				clock = new VectorClock(numOfNode);
		default:
			clock = null;
		}
		return clock;
	}
	
	abstract TimeStamp getTimeStamp();
}
