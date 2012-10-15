package nl.rug.peerbox.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {
	
	public InetAddress address;
	public int port;
	
	public static Host byIpAndPort(byte[] ip, int port) {
		Host h = new Host();
		try {
			h.address = InetAddress.getByAddress(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		h.port = port;
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Host)) {
			return false;
		}
		
		Host other = (Host)obj;
		
		return (address.equals(other.address) && port == other.port);
	}
	
	@Override
	public int hashCode() {
		return address.hashCode() + port + 23;
	}
	
	@Override
	public String toString() {
		return address.getHostAddress() + ":"+port;
	}
}
