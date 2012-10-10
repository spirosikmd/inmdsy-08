package nl.rug.peerbox.middleware;

import java.util.HashMap;

abstract class MessageHandler {
	
	private static HashMap<Byte, MessageHandler> handler = new HashMap<Byte,MessageHandler>();
	
	protected static void register(byte messagetype, MessageHandler mh) {
		handler.put(messagetype, mh);
	}
	
	static void handle(MulticastMessage m) {
		handler.get(m.getCommand()).processMessage(m);
	}
	
	abstract void processMessage(MulticastMessage m);
}
