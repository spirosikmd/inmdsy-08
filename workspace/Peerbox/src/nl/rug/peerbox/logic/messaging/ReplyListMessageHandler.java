package nl.rug.peerbox.logic.messaging;

import java.util.Collection;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.PeerboxFile;
import nl.rug.peerbox.logic.VirtualFileSystem;
import nl.rug.peerbox.logic.messaging.Message.Key;

final class ReplyListMessageHandler extends MessageHandler {

	//private static final Logger logger = Logger.getLogger(ReplyListMessageHandler.class);

	@SuppressWarnings("unchecked")
	@Override
	final void handle(Message message, Context ctx) {
		VirtualFileSystem vfs = ctx.getVirtualFilesystem();
		Collection<PeerboxFile> messageFilelist = (Collection<PeerboxFile>) message
				.get(Key.Files);
		for (PeerboxFile file : messageFilelist) {
			vfs.addFile(file);

		}

	}
}
