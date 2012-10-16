package nl.rug.peerbox.logic.handler;

import java.util.HashMap;
import java.util.Map;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peerbox;
import nl.rug.peerbox.logic.PeerboxMessage;
import nl.rug.peerbox.logic.UnsupportedCommandException;

public abstract class MessageHandler {
	
	private static Map<Object, MessageHandler> handlers = new HashMap<Object, MessageHandler>();
	
	static void registerHandler(final MessageHandler handler,final Object forCommand) {
		handlers.put(forCommand, handler);
	}
	
	public static void process(final PeerboxMessage message, final Context ctx) {
		Object command = message.get(Peerbox.KEY_COMMAND);
		if (command != PeerboxMessage.NULLOBJ) {
			if (handlers.containsKey(command)) {
			handlers.get(command).handle(message, ctx);
			} else {
				throw new UnsupportedCommandException();
			}
		}
	}
	
	abstract void handle(final PeerboxMessage message,final  Context ctx);
	
}
