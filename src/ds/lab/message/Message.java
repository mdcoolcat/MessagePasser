package ds.lab.message;


import java.io.Serializable;

public class Message implements Serializable, Cloneable {

	/**
	 * header and payload
	 */
	private static final long serialVersionUID = 1L;
	
	protected Header header = null;
	protected Object data = null;	
	
	public Message() {
		this.header = new Header("", "", null);
		setId(-1);//for initial use
	}
	public Message(String src, String dest, String kind, Object data) {
		assert src != null;
		assert dest != null;
		//TODO assert kind != null;
		this.header = new Header(src, dest, kind);
		this.data = data;
	}

	public int getId() {
		return header.id;
	}
	
	public void setId(int id) {
		header.id = id;
	}
	
	public String getDest() {
		return header.dest;
	}
	public String getSrc() {
		return header.src;
	}
	public String getKind() {
		return header.kind;
	}
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public Message clone() throws CloneNotSupportedException {
		return new Message(getSrc(), getDest(), getKind(), data);
	}
	
	@Override
	public String toString() {
		return this.header + "\n" + data;
	}


	class Header implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2L;
		int id;
		String src;
		String dest;
		String kind;
		
		public Header(String src, String dest, String kind) {
			this.src = src;
			this.dest = dest;
			this.kind = kind;
		}
		
		@Override
		public String toString() {
			return "Message " + this.id + ", From " + this.src + " To " + this.dest + " kind: " + kind;
		}
	}


//	public static MessageKind getMessageKind(int k) {
//			MessageKind mk=null;
//			switch(k){
//			case 0: return MessageKind.LOOKUP;
//			case 1: return MessageKind.ACK;
//			case 2: return MessageKind.NONE;
//			default: return MessageKind.NONE;
//			}
//	}
//	public static int getMessageKindIndex(String str) {
//		switch(str.toLowerCase()){
//		case "lookup": return 0;
//		case "ack": return 1;
//		case "none": return 2;
//		default: return 2;
//		}
//}
	
}
