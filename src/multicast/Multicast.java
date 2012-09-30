import java.io.*;
import java.net.*;
import java.util.UUID;

public class Multicast {

	public static void main(String[] args) throws IOException {
		if ((args.length != 1) || (args[0].indexOf(":") < 0))
			throw new IllegalArgumentException(
				"Syntax: Multicast <group>:<port>");

		int idx = args[0].indexOf(":");
		InetAddress group = InetAddress.getByName(args[0].substring(0, idx));
		int port = Integer.parseInt(args[0].substring(idx + 1));

		ReadSource readSource = new ReadSource();
		Peer peer = new Peer(group, port);
		
		readSource.addEventListener(peer);
		
		peer.start();
	}
}