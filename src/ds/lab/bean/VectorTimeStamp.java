package ds.lab.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VectorTimeStamp extends TimeStamp<VectorTimeStamp> implements Serializable {
	// Not required
	/*
	 * private int numOfNode; private AtomicIntegerArray timestamp;
	 */
	final static int LESS_THAN = -1;
	final static int GREATER_THAN = 1;
	final static int EQUAL_TO = 0;
	private static final long serialVersionUID = 5413090594730940192L;
	private HashMap<String, AtomicInteger> vector;

	public VectorTimeStamp() {
		vector = new HashMap<String, AtomicInteger>();
	}

	public HashMap<String, AtomicInteger> getVector() {
		return this.vector;
	}

	public void setVector(HashMap<String, AtomicInteger> vector) {
		this.vector = vector;
	}

	@Override
	public int compareTo(VectorTimeStamp o) {
		boolean less = false;
		boolean greater = false;
		ArrayList<AtomicInteger> myValue = new ArrayList<AtomicInteger>(this.getVector().values());
		ArrayList<AtomicInteger> othersValue = new ArrayList<AtomicInteger>(o.getVector().values());
//		System.out.println(myValue);
//		System.out.println(othersValue);
		for (int i = 0; i < myValue.size(); i++) {
			int my = myValue.get(i).get();
			int other = othersValue.get(i).get();
			if (my < other)
				less = true;
			else if (my > other)
				greater = true;
		}
		if (less && !greater)
			return -1;
		if (greater && !less)
			return 1;
		// if (!less && !greater)
		return 0;

		// TODO compare this to o. if this before o, return -1 (less
		// than)...assuming == is concurrent
		// HashMap<String,Integer> comparison=new HashMap<String,Integer>();
		// final int LESS_THAN=-1;
		// final int GREATER_THAN=1;
		// final int EQUAL_TO=0;
		// for(Map.Entry<String, AtomicInteger> node:
		// this.getVector().entrySet())
		// {
		// String myName=node.getKey();
		// int
		// comparedResult=node.getValue().get()-o.getVector().get(myName).get();
		// comparison.put(myName, Integer.valueOf(comparedResult));
		// }
		// boolean lessThanFlag=true;
		// boolean greaterThanFlag=true;
		// boolean equalToFlag=true;
		// Iterator<Integer> i = comparison.values().iterator();
		// while(i.hasNext())
		// {
		// int value=((Integer) i.next()).intValue();
		// if(value==LESS_THAN)
		// lessThanFlag=lessThanFlag&true;
		// else
		// lessThanFlag=lessThanFlag&false;
		// if(value==GREATER_THAN)
		// greaterThanFlag=greaterThanFlag&true;
		// else
		// greaterThanFlag=greaterThanFlag&false;
		// if(value==EQUAL_TO)
		// equalToFlag=equalToFlag&true;
		// else
		// equalToFlag=equalToFlag&false;
		// }
		// if(lessThanFlag==true)
		// return -1;
		// if(equalToFlag==true)
		// return 0;
		// if(greaterThanFlag==true)
		// return 1;
		// return 0;
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();

		for (java.util.Map.Entry<String, AtomicInteger> e : vector.entrySet())

			sb.append(e.getKey() + "-" + e.getValue() + " ");

		return sb.toString();
	}

	private int compare(int m, int n) {
		int result = m - n;
		if (result < 0)
			return LESS_THAN;
		if (result > 0)
			return GREATER_THAN;
		else
			return EQUAL_TO;
	}

}
