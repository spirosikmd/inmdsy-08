package nl.rug.peerbox.logic;

import java.util.ArrayList;

import nl.rug.peerbox.middleware.MulticastGroup;

public interface Context {
	
	public MulticastGroup getMulticastGroup();
	
	public String getPathToPeerbox();

	public byte[] getIP();

	public int getPort();
	
	public ArrayList<FileDescriptor> getVirtualFilesystem(); // Map<Host, String[]>
	
}
