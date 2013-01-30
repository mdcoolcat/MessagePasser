package ds.lab.message;

import ds.lab.bean.TimeStamp;

public class TimeStampMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		//return new TimeStampMessage(getSrc(), getDest(), getKind(), data);
		return msg;
	}
	
	@Override
	public String toString() {
		return timeStamp + " " + super.toString();
	}

}
