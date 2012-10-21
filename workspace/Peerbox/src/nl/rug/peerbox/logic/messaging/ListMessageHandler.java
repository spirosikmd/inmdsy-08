package nl.rug.peerbox.logic.messaging;

import java.util.Collection;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.messaging.Message.Command;
import nl.rug.peerbox.logic.messaging.Message.Key;
import nl.rug.peerbox.logic.PeerboxFile;

final class ListMessageHandler extends MessageHandler {

	@Override
	void handle(Message message, Context ctx) {

		Collection<PeerboxFile> files = ctx.getVirtualFilesystem().getFileList();
		Message reply = new Message();
		reply.put(Key.Command, Command.Reply.List);
		reply.put(Key.Files, files);
		reply.put(Key.Peer, ctx.getLocalPeer());
		ctx.getMulticastGroup().announce(reply.serialize());

	}

}