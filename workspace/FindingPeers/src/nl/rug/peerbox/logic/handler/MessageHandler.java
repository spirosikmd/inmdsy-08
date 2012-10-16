package nl.rug.peerbox.logic.handler;

import java.util.HashMap;
import java.util.Map;

import nl.rug.peerbox.logic.Peerbox;
import nl.rug.peerbox.logic.PeerboxMessage;
import nl.rug.peerbox.logic.UnsupportedCommandException;

public abstract class MessageHandler {
	
	private static Map<Object, MessageHandler> handlers = new HashMap<Object, MessageHandler>();
	
	static void registerHandler(MessageHandler handler, Object forCommand) {
		handlers.put(forCommand, handler);
	}
	
	public static void process(PeerboxMessage message) {
		Object command = message.get(Peerbox.KEY_COMMAND);
		if (command != PeerboxMessage.NULLOBJ) {
			if (handlers.containsKey(command)) {
			handlers.get(command).handle(message);
			} else {
				throw new UnsupportedCommandException();
			}
		}
	}
	
	abstract void handle(PeerboxMessage message);
	
}
