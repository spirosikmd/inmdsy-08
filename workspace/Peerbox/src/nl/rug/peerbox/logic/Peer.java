package nl.rug.peerbox.logic;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InetAddress address;
	public int port;

	public static Peer byIpAndPort(byte[] ip, int port) {
		Peer h = new Peer();
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
		if (obj == null || !(obj instanceof Peer)) {
			return false;
		}

		Peer other = (Peer) obj;

		return (address.equals(other.address) && port == other.port);
	}

	@Override
	public int hashCode() {
		return address.hashCode() + port + 23;
	}

	@Override
	public String toString() {
		return address.getHostAddress() + ":" + port;
	}
}
