package nl.rug.peerbox.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.rug.peerbox.middleware.HostListener;
import nl.rug.peerbox.middleware.Multicast;

public class PeerManager implements HostListener {

	private final ConcurrentHashMap<Integer, Peer> peers = new ConcurrentHashMap<Integer, Peer>();
	private final List<PeerListener> listener = new ArrayList<PeerListener>();

	public PeerManager(Multicast multicast) {
		multicast.getHostManager().addListener(this);
	}

	public void addPeerListener(PeerListener l) {
		synchronized (listener) {
			listener.add(l);
		}
	}

	public void removePeerListener(PeerListener l) {
		synchronized (listener) {
			listener.remove(l);
		}
	}

	@Override
	public void removed(int hostID) {
		Peer p = peers.remove(hostID);
		if (p != null) {
			synchronized (listener) {
				for (PeerListener l : listener) {
					l.deleted(hostID, p);
				}
			}
		}
	}

	public void updatePeer(int hostID, Peer peer) {
		peers.put(hostID, peer);
		synchronized (listener) {
			for (PeerListener l : listener) {
				l.updated(hostID, peer);
			}
		}
	}

	@Override
	public void detected(int hostID) {
		synchronized (listener) {
			for (PeerListener l : listener) {
				l.updated(hostID, peers.get(hostID));
			}
		}
	}

	public void removePeer(Peer peer) {
		int hostID = -1;
		for (Map.Entry<Integer, Peer> entry : peers.entrySet()) {
			if (entry.getValue().equals(peer)) {
				hostID = entry.getKey();
			}
		}
		if (peers.remove(hostID) != null) {
			synchronized (listener) {
				for (PeerListener l : listener) {
					l.deleted(hostID, peer);
				}
			}
		}
	}
}
