package ds.lab.message;

import ds.lab.bean.TimeStamp;

public class TimeStampMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected TimeStampMessage myDup = null;
	
	public TimeStampMessage(String src, String dest, String kind, Object data) {
		super(src, dest, kind, data);
	}

	private TimeStamp timeStamp;

	public TimeStamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(TimeStamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	@Override
	public TimeStampMessage clone() throws CloneNotSupportedException {
		TimeStampMessage msg = new TimeStampMessage(this.getSrc(), this.getDest(), getKind(), this.getData());
		msg.setTimeStamp(this.getTimeStamp());
		this.myDup = msg;
		return msg;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeStampMessage other = (TimeStampMessage) obj;
		if (this.myDup != null)
			return this.myDup == other;
		else
			return this.getId() == other.getId() && this.getSrc().equals(other.getSrc());//<src, id> unique
	}

	@Override
	public String toString() {
		return timeStamp + " |";
	}

}
