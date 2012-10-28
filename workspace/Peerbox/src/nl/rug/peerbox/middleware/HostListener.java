package nl.rug.peerbox.middleware;

public interface HostListener {
	
	void detected(RemoteHost h);
	void removed(RemoteHost h);

}
