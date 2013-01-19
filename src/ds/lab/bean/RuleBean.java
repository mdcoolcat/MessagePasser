package ds.lab.bean;

import ds.lab.message.Message;
import ds.lab.message.MessageAction;
import ds.lab.message.MessageKind;

public class RuleBean {
	/** must have */
	private MessageAction action;
	/** optional,used for rule check */
	private String src;
	private String dest;
	private MessageKind kind;
	private int id = -1;
	/** optional */
	private int nth;
	private int everyNth;
	
	public RuleBean() {};
	
	public RuleBean(MessageAction action) {
		super();
		this.action = action;
	}

	public boolean isDrop() {
		return this.action == MessageAction.DROP;
	}
	
	public boolean isDelay() {
		return this.action == MessageAction.DELAY;
	}
	
	public boolean isDuplicate() {
		return this.action == MessageAction.DUPLICATE;
	}
	public boolean isMatch(Message m) {
//		boolean match = true;
		if (this.id != -1 && this.id != m.getId())
			return false;
		if (this.src != null && !this.src.equals(m.getSrc()))
			return false;
		if (this.dest != null && !this.dest.equals(m.getDest()))
			return false;
		if (this.kind != null && !this.kind.equals(m.getKind()))
			return false;
		return true;
	}
	
	public MessageAction getAction() {
		return action;
	}

	//TODO change to string if parse is difficult for this type
	public void setAction(MessageAction action) {
		this.action = action;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public MessageKind getKind() {
		return kind;
	}

	//TODO change to string if parse is difficult for this type
	public void setKind(MessageKind kind) {
		this.kind = kind;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEveryNth() {
		return everyNth;
	}

	public void setEveryNth(int everyNth) {
		this.everyNth = everyNth;
	}
	
	public int getNth() {
		return nth;
	}

	public void setNth(int nth) {
		this.nth = nth;
	}


}
