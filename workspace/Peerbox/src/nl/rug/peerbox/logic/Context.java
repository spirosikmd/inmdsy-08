package nl.rug.peerbox.logic;

import java.util.ArrayList;

import nl.rug.peerbox.middleware.Multicast;

public interface Context {
	
	public Multicast getMulticastGroup();
	
	public String getPathToPeerbox();

	public byte[] getIP();

	public int getPort();
	
<<<<<<< HEAD:workspace/FindingPeers/src/nl/rug/peerbox/logic/Context.java
	public ArrayList<FileDescriptor> getVirtualFilesystem(); // Map<Host, String[]>
=======
	public Map<Peer, String[]> getVirtualFilesystem();
>>>>>>> c39c4675943e89f08b1d029a6d45eab7463cbb03:workspace/Peerbox/src/nl/rug/peerbox/logic/Context.java
	
}
