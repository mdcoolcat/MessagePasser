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
	
	public NodeBean(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		try {
			this.inetAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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
	
	@Override
	public String toString() {
		return name + " " + ip + " " + port;
	}
	
}
