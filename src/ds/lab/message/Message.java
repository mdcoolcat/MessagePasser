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
//		assert src != null;
//		assert dest != null;
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
	public void setDest(String newDest) {
		header.dest = newDest;
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
			return this.id + " | " + this.src + " | " + this.dest + " " +
					"| " + kind;
		}
	}
}
