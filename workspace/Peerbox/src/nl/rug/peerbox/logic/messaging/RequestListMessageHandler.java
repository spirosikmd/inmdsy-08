package nl.rug.peerbox.logic.messaging;

import java.util.Collection;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.PeerboxFile;
import nl.rug.peerbox.logic.VirtualFileSystem;
import nl.rug.peerbox.logic.messaging.Message.Command;
import nl.rug.peerbox.logic.messaging.Message.Key;

final class RequestListMessageHandler extends MessageHandler {

	@SuppressWarnings("unchecked")
	@Override
	void handle(Message message, Context ctx) {

		Collection<PeerboxFile> files = ctx.getVirtualFilesystem()
				.getFileList();
		Message reply = new Message();
		reply.put(Key.Command, Command.Reply.List);
		reply.put(Key.Files, files);
		reply.put(Key.Peer, ctx.getLocalPeer());
		ctx.getMulticastGroup().announce(reply.serialize());

		Collection<PeerboxFile> messageFilelist = (Collection<PeerboxFile>) message
				.get(Key.Files);
		if (messageFilelist != null) {
			VirtualFileSystem vfs = ctx.getVirtualFilesystem();
			for (PeerboxFile file : messageFilelist) {
				vfs.addFile(file);
			}
		}
	}

}