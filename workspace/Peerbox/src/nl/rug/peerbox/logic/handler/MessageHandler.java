package nl.rug.peerbox.logic.handler;

import java.util.HashMap;
import java.util.Map;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.handler.Message.Command;
import nl.rug.peerbox.logic.handler.Message.Key;

import org.apache.log4j.Logger;

public abstract class MessageHandler {

	private static Map<Object, MessageHandler> handlers = new HashMap<Object, MessageHandler>();
	private static final Logger logger = Logger.getLogger(MessageHandler.class);

	static {
		registerHandler(new ReplyToListMessageHandler(), Command.Reply.List);
		registerHandler(new ListMessageHandler(), Command.Request.List);
		registerHandler(new EventsMessageHandler(), Command.Info.Events);
	}
	
	static void registerHandler(final MessageHandler handler,
			final Object forCommand) {
		logger.info("Register message handler for the command " + forCommand.getClass().getSimpleName()  + " " +forCommand );
		handlers.put(forCommand, handler);
	}

	public static void process(final Message message, final Context ctx) throws UnsupportedCommandException {
		Object command = message.get(Key.Command);
		if (command != Message.NULLOBJ) {
			if (handlers.containsKey(command)) {
				logger.debug("Process command " + command.getClass().getSimpleName() + " " + command);
				handlers.get(command).handle(message, ctx);
			} else {
				throw new UnsupportedCommandException(command);
			}
		} else {
			logger.debug("No command key found");
		}
	}

	abstract void handle(final Message message, final Context ctx);

}
