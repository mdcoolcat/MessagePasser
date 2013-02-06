package ds.lab.message;


public class MulticastMessage extends TimeStampMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int multiccastId;
	
	private String origin;
	private MulticastType type;

//	public MulticastMessage(String src, String dest, String kind, Object data) {
//		super(src, dest, kind, data);
//	}

//	public MulticastMessage(String src, String forward, String dest, String kind, MulticastType type, Object data) {
//		super(src, dest, kind, data);
//		this.forward = forward;
//		this.type = type;
//	}

	/**
	 * 
	 * @param origin
	 * @param src
	 * @param dest
	 * @param kind
	 * @param type
	 * @param data
	 * @param ts
	 */
	public MulticastMessage(String origin, String src, String dest, String kind, MulticastType type, Object data) {
		super(src, dest, kind, data);
		this.origin = origin;
		this.type = type;
//		this.setTimeStamp(ts);
	}

	public MulticastMessage(int multicastId, String origin, String src, String dest, String kind, MulticastType type, Object data) {
		this(origin, src, dest, kind , type, data);
		this.multiccastId = multicastId;
	}

	public int getMulticcastId() {
		return multiccastId;
	}

	public void setMulticcastId(int multiccastId) {
		this.multiccastId = multiccastId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public MulticastType getType() {
		return type;
	}

	public void setType(MulticastType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return this.multiccastId + " | " + this.type + "| " + this.getOrigin() + "->" +this.getSrc() + "->" + this.getDest();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + multiccastId;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		MulticastMessage other = (MulticastMessage) obj;
		if (this.myDup != null)
			return this.myDup == other;
		else
			return this.getMulticcastId() == other.getMulticcastId() && this.getOrigin().equals(other.getOrigin());//<src, id> unique
	
	}

	@Override
	public MulticastMessage clone() throws CloneNotSupportedException {
		MulticastMessage msg = new MulticastMessage(getMulticcastId(), getOrigin(), getSrc(), getDest(), getKind(), type, data);
		msg.setTimeStamp(getTimeStamp());
		return msg;
	}

	
	
}
