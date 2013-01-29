package ds.lab.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class LogicalTimeStamp extends TimeStamp<LogicalTimeStamp> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	//	private AtomicInteger d;
	private HashMap<String,AtomicInteger> vector;

	public LogicalTimeStamp() {
		vector=new HashMap<String,AtomicInteger>();
	}


	public AtomicInteger getD() {
		Iterator<AtomicInteger> i = vector.values().iterator();
		return i.next();
	}
//Not Required
/*
	public void setD(int d) {
		this.d.set(d);
	}
*/
	public  HashMap<String,AtomicInteger> getVector()
	{
		return this.vector;
	}
	
	@Override
	public synchronized int compareTo(LogicalTimeStamp o) {
		// TODO compare this to o. if this before o, return -1 (less than)...
		int me = this.getD().get();
		int other = o.getD().get();
		if (me < other)
			return -1;
		else if (me > other)
			return 1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return getD().toString();
	}

}
