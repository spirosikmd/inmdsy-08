package nl.rug.peerbox.logic.handler;

import java.util.ArrayList;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.FileDescriptor;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Key;
import nl.rug.peerbox.logic.Peer;

final class ReplyToListMessageHandler extends MessageHandler {

	@Override
	final void handle(Message message, Context ctx) {

		ArrayList<FileDescriptor> files = (ArrayList<FileDescriptor>) message
				.get(Key.Files);
		Object obj = message.get(Key.Peer);
		if (obj instanceof Peer) {
			ctx.getVirtualFilesystem().getFileList().addAll(files);
		}

	}

}
