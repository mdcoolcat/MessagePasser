package ds.lab.message;

import java.io.Serializable;

import ds.lab.bean.TimeStamp;

public class TimeStampMessage extends Message implements Serializable {

//	public TimeStampMessage() {
//		super();
//	}

	public TimeStampMessage(String src, String dest, String kind, Object data) {
		super(src, dest, kind, data);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8573893575075541922L;

	private TimeStamp timeStamp;

	public TimeStamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(TimeStamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@Override
	public TimeStampMessage clone() throws CloneNotSupportedException {
//		TimeStampMessage msg = (TimeStampMessage) super.clone();
//		msg.setTimeStamp(getTimeStamp());
		return new TimeStampMessage(getSrc(), getDest(), getKind(), data);
	}

}
