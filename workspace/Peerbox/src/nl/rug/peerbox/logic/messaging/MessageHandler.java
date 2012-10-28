package nl.rug.peerbox.logic.messaging;

import java.util.HashMap;
import java.util.Map;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.messaging.Message.Command;
import nl.rug.peerbox.logic.messaging.Message.Key;

import org.apache.log4j.Logger;

public abstract class MessageHandler {

	private static Map<Object, MessageHandler> handlers = new HashMap<Object, MessageHandler>();
	private static final Logger logger = Logger.getLogger(MessageHandler.class);

	static {
		registerHandler(new ReplyListMessageHandler(), Command.Reply.List);
		registerHandler(new RequestListMessageHandler(), Command.Request.List);
		registerHandler(new InfoCreatedMessageHandler(), Command.Info.Created);
		registerHandler(new InfoDeletedMessageHandler(), Command.Info.Deleted);
		registerHandler(new HeartbeatMessageHandler(), Command.Info.Heartbeat);
	}

	static void registerHandler(final MessageHandler handler,
			final Object forCommand) {
		logger.info("Register message handler for the command "
				+ forCommand.getClass().getSimpleName() + " " + forCommand);
		handlers.put(forCommand, handler);
	}


	public static void process(final Message message, final Context ctx) throws UnsupportedCommandException {
		Object command = message.get(Key.Command);
		Object obj = message.get(Key.Peer);
		if (obj != Message.NULLOBJ && obj instanceof Peer) {
			Peer peer = (Peer)obj;
			//TODO stuff
		}
		if (command != Message.NULLOBJ) {
			if (handlers.containsKey(command)) {
				logger.debug("Process command "
						+ command.getClass().getSimpleName() + " " + command);
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
