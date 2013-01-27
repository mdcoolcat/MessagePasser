package ds.lab.bean;

import java.util.concurrent.atomic.AtomicInteger;

public class TimeStamp implements Comparable<TimeStamp>{
	private AtomicInteger d;

	public AtomicInteger getD() {
		return d;
	}

	public void setD(int d) {
		this.d.set(d);
	}

	@Override
	public int compareTo(TimeStamp o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
