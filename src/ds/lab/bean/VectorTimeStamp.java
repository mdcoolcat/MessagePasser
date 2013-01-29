package ds.lab.bean;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VectorTimeStamp extends TimeStamp<VectorTimeStamp> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;
	/*
	 * private int numOfNode;
	 private AtomicIntegerArray timestamp;
	*/
	private HashMap<String,AtomicInteger> vector;

	public VectorTimeStamp()
	{
		vector=new HashMap<String,AtomicInteger>();
	}
	public  HashMap<String,AtomicInteger> getVector()
	{
		return this.vector;
	}
	
	@Override
	public int compareTo(VectorTimeStamp o) {
		// TODO compare this to o. if this before o, return -1 (less than)...assuming == is concurrent
		
		
		return 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (java.util.Map.Entry<String, AtomicInteger> e : vector.entrySet())
			sb.append(e.getKey()+": "+e.getValue());
		return sb.toString();
	}
	public void setVector(HashMap<String, AtomicInteger> vector) {
		this.vector = vector;
	}

}
