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
	private int nth = -1;
	private int everyNth = -1;
	
	public RuleBean() {};
	
	public RuleBean(MessageAction action) {
		super();
		this.action = action;
	}

	/**
	 * check if the incoming message match sendRule
	 * @param m
	 * @return
	 */
	public boolean isMatch(Message m) {
		assert action != null;
		if (src == null && dest == null && (kind == null || kind == MessageKind.NONE) && id == -1)	//only contains action, it matches all msg
			return true;
		if (src != null && !src.equals(m.getSrc()))
			return false;
		if (dest != null && !dest.equals(m.getDest()))
			return false;
		if (kind != null && !kind.equals(m.getKind()))
			return false;
		if (id != -1 && id != m.getId())
			return false;
		return true;
	}

	public boolean hasNoRestriction() {
		assert action != null;
		return (nth < 0) && (everyNth < 0);
	}
	
	public MessageAction getAction() {
		return action;
	}
	public int getActionIndex() {
		switch (action) {
		case DROP:return 0;
		case DUPLICATE:return 1;
		case DELAY:return 2;
		}
		System.err.println("ERROR: Unsupported action");
		return -1;
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

	@Override
	public String toString() {
		return dest+" nth:"+nth+" everyN:"+everyNth+" "+action;
	}

}
