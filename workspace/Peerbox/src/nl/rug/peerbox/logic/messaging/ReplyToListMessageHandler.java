package nl.rug.peerbox.logic.messaging;

import java.util.Collection;

import org.apache.log4j.Logger;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.messaging.Message.Key;
import nl.rug.peerbox.logic.Peer;
import nl.rug.peerbox.logic.PeerboxFile;
import nl.rug.peerbox.logic.VirtualFileSystem;

final class ReplyToListMessageHandler extends MessageHandler {
	
	private static final Logger logger = Logger.getLogger(ReplyToListMessageHandler.class);
	
	@Override
	final void handle(Message message, Context ctx) {

		

		VirtualFileSystem vfs = ctx.getVirtualFilesystem();
		Collection<PeerboxFile> messageFilelist = (Collection<PeerboxFile>) message
				.get(Key.Files);
		Object obj = message.get(Key.Peer);
		System.out.println("I received " + messageFilelist.size());
			for (PeerboxFile file : messageFilelist) {
				logger.info("Received info on file " + file.getFilename());
				vfs.addFile(file);
		
			}
		

	}
}
