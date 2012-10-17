package nl.rug.peerbox.logic.handler;

import java.util.HashMap;
import java.util.Map;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.PeerboxMessage;
import nl.rug.peerbox.logic.UnsupportedCommandException;
import nl.rug.peerbox.logic.PeerboxMessage.Key;

import org.apache.log4j.Logger;

public abstract class MessageHandler {

	private static Map<Object, MessageHandler> handlers = new HashMap<Object, MessageHandler>();
	private static final Logger logger = Logger.getLogger(MessageHandler.class);

	static {
		registerHandler(new ReplyToListMessageHandler(), "LISTREPLY");
		registerHandler(new ListMessageHandler(), "LIST");
	}
	
	static void registerHandler(final MessageHandler handler,
			final Object forCommand) {
		logger.info("Register message handler for the command " + forCommand );
		handlers.put(forCommand, handler);
	}

	public static void process(final PeerboxMessage message, final Context ctx) throws UnsupportedCommandException {
		Object command = message.get(Key.Command);
		if (command != PeerboxMessage.NULLOBJ) {
			if (handlers.containsKey(command)) {
				logger.debug("Process command " + command);
				handlers.get(command).handle(message, ctx);
			} else {
				throw new UnsupportedCommandException(command);
			}
		} else {
			logger.debug("No command key found");
		}
	}

	abstract void handle(final PeerboxMessage message, final Context ctx);

}
