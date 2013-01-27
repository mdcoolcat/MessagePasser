package ds.lab.messagepasser;

import ds.lab.bean.TimeStamp;

public class VectorClock extends ClockService {
	private int n;//number of nodes in the system
	private int[] timeStamps;
	VectorClock(int n) {
		//TODO constructor, init d value, vector 
		this.n = n;
		this.timeStamps = new int[n];
	}
	@Override
	TimeStamp getTimeStamp() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
