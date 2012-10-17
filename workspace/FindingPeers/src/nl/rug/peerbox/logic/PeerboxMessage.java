package nl.rug.peerbox.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class PeerboxMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Object NULLOBJ = new Object();
	private static final Logger logger = Logger.getLogger(PeerboxMessage.class);
	private final Map<Key, Object> dictionary = new HashMap<Key, Object>();

	public static enum Key {
		Command,
		Files,
		IP,
		Port
	}

	public void put(Key key, Object obj) {
		dictionary.put(key, obj);
	}

	public Object get(Key key) {
		Object result = NULLOBJ;
		if (dictionary.containsKey(key)) {
			result = dictionary.get(key);
		}
		return result;
	}

	public byte[] serialize() {
		byte[] data = new byte[0]; 
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);
			os.writeObject(this);
			data = baos.toByteArray();
		} catch (IOException e) {
			logger.error(e);
		}
		return data;
	}
	
	public static PeerboxMessage deserialize(byte[] data) {
		PeerboxMessage message = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(bais);
			Object o = is.readObject();
			if (o instanceof PeerboxMessage) {
				message = (PeerboxMessage)o;
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.error(e);
		}
		return message;
	}

}
