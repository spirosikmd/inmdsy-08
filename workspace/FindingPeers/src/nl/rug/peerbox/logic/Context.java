package nl.rug.peerbox.logic;

import java.util.Map;

import nl.rug.peerbox.middleware.MulticastGroup;

public interface Context {
	
	public MulticastGroup getMulticastGroup();
	
	public String getPathToPeerbox();

	public byte[] getIP();

	public int getPort();
	
	public Map<Host, String[]> getVirtualFilesystem();
	
}
