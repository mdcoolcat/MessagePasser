package ds.lab.message;

import java.io.Serializable;

import ds.lab.bean.TimeStamp;

public class TimeStampMessage extends Message implements Serializable {

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
		TimeStampMessage msg = (TimeStampMessage) super.clone();
		msg.setTimeStamp(getTimeStamp());
		return msg;
	}

}
