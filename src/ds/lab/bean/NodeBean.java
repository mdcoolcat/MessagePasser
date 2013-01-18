package ds.lab.bean;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2241585193822254252L;
	private String name;
	private String ip;
	private InetAddress inetAddr;
	private int port;
	
	public NodeBean() {
		
	}
	
	public NodeBean(String name, String ip, int port) throws UnknownHostException {
		this.name = name;
		this.ip = ip;
		this.inetAddr = InetAddress.getByName(ip);
		this.port = port;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) throws UnknownHostException {
		this.ip = ip;
		this.inetAddr = InetAddress.getByName(ip);
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	
}
