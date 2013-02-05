package ds.lab.message;

import ds.lab.bean.TimeStamp;

public class TimeStampMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TimeStampMessage myDup = null;
	
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
		return this.myDup == other;
//		if (this.getId() != other.getId())
//			return false;
//		if (!this.getSrc().equals(other.getSrc()))
//			return false;
//		if (!this.getDest().equals(other.getSrc()))
//			return false;
//		if (!this.getKind().equals(other.getKind()))
//				return false;
//		if (!this.getData().equals(other.getData()))
//				return false;
//		return true;
	}

	@Override
	public String toString() {
		return timeStamp + " " + super.toString();
	}

}
