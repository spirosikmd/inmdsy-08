package nl.rug.peerbox.logic.handler;

import java.util.ArrayList;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.FileDescriptor;
import nl.rug.peerbox.logic.PeerboxMessage;

final class ReplyToListMessageHandler extends MessageHandler {

	static {
		registerHandler(new ReplyToListMessageHandler(), "LISTREPLY");
	}

	@Override
	final void handle(PeerboxMessage message, Context ctx) {

		ArrayList<FileDescriptor> filelist = (ArrayList<FileDescriptor>) message
				.get("FILES");

		ctx.getVirtualFilesystem().addAll(filelist);
	}

}
