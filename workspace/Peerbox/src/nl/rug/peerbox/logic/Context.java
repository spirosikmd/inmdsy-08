package nl.rug.peerbox.logic;

import java.util.Map;

import nl.rug.peerbox.middleware.Multicast;

public interface Context {
	
	public Multicast getMulticastGroup();
	
	public String getPathToPeerbox();

	public byte[] getIP();

	public int getPort();
	

	//public ArrayList<FileDescriptor> getVirtualFilesystem(); // Map<Host, String[]>

	public Map<Peer, String[]> getVirtualFilesystem();

	
}
