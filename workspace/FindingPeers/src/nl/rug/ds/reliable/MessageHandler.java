package nl.rug.ds.reliable;

import java.util.HashMap;

abstract class MessageHandler {
	
	private static HashMap<Byte, MessageHandler> handler = new HashMap<Byte,MessageHandler>();
	
	protected static void register(byte messagetype, MessageHandler mh) {
		handler.put(messagetype, mh);
	}
	
	static void handle(Message m) {
		handler.get(m.getCommand()).processMessage(m);
	}
	
	abstract void processMessage(Message m);
}
