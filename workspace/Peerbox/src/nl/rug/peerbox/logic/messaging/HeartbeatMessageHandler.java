package nl.rug.peerbox.logic.messaging;

import java.sql.Date;
import java.util.HashMap;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.UFID;
import nl.rug.peerbox.logic.messaging.Message.Key;

public class HeartbeatMessageHandler extends MessageHandler {

	static HashMap<Peer, Long> peerbeats = new HashMap<Peer, Long>();
	
	@Override
	void handle(Message message, Context ctx) {
		//todo heartbeat
		Object o = message.get(Key.Peer);
		if (o instanceof Peer) {
			Peer p = (Peer)o;
			peerbeats.put(p, new java.util.Date().getTime());
		}

	}

}
