package nl.rug.peerbox.logic;

import java.util.Properties;

import nl.rug.peerbox.middleware.Multicast;

public interface Context {

	public Multicast getMulticastGroup();

	public String getPathToPeerbox();

	public Peer getLocalPeer();

	public VirtualFileSystem getVirtualFilesystem();
	
	public Properties getProperties();

}
