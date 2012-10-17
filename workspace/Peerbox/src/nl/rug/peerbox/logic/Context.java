package nl.rug.peerbox.logic;

import java.util.Map;

import nl.rug.peerbox.middleware.Multicast;

public interface Context {
	
	public Multicast getMulticastGroup();
	
	public String getPathToPeerbox();
	
	public Peer getLocalPeer();
	

	//public ArrayList<FileDescriptor> getVirtualFilesystem(); // Map<Host, String[]>

	public Map<Peer, String[]> getVirtualFilesystem();

	
}
