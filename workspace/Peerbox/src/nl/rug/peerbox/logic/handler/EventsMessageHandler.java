package nl.rug.peerbox.logic.handler;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Key;

public class EventsMessageHandler extends MessageHandler {

	@Override
	void handle(Message message, Context ctx) {

		List<WatchEvent<?>> events = (List<WatchEvent<?>>) message
				.get(Key.Events);

		for (WatchEvent<?> event : events) {
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				System.out.println("Created: " + event.context().toString());
			}
			if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				System.out.println("Delete: " + event.context().toString());
			}
			if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				System.out.println("Modify: " + event.context().toString());
			}
		}

	}

}
