package nl.rug.peerbox.logic.handler;

import java.util.concurrent.ConcurrentHashMap;

import nl.rug.peerbox.logic.Context;
import nl.rug.peerbox.logic.Message;
import nl.rug.peerbox.logic.Message.Command;
import nl.rug.peerbox.logic.Message.Key;
import nl.rug.peerbox.logic.PeerboxFile;
import nl.rug.peerbox.logic.UFID;

final class ListMessageHandler extends MessageHandler {

	@Override
	void handle(Message message, Context ctx) {

		ConcurrentHashMap<UFID, PeerboxFile> files = ctx.getVirtualFilesystem()
				.getFileList();

		Message reply = new Message();
		reply.put(Key.Command, Command.Reply.List);
		reply.put(Key.Files, files);
		reply.put(Key.Peer, ctx.getLocalPeer());
		ctx.getMulticastGroup().announce(reply.serialize());

	}

}