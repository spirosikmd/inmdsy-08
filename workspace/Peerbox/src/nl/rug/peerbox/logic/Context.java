package nl.rug.peerbox.logic;

import nl.rug.peerbox.middleware.Multicast;

public interface Context {

	Multicast getMulticastGroup();

	String getPathToPeerbox();

	Peer getLocalPeer();

	VirtualFileSystem getVirtualFilesystem();

	String getDatafileName();

	void join();

	void leave();

	void requestFiles(boolean initFilelist);
	
	void addPeerListener(PeerListener l);
	void removePeerListener(PeerListener l);

	void peerLeft(Peer peer);

}
