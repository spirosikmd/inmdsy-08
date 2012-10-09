package nl.rug.ds.reliable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Uses a CRC32 checksum check for integrity
 * 
 * @author cm
 * 
 */
final class Message {

	static final byte SEND = 1;
	static final byte ACK = 2;
	static final byte MISS = 4;

	static final int HEADER_SIZE = 19;

	private byte command;
	private int source;
	private int messageID;
	private long checksum;
	private short length;
	private byte[] payload = new byte[0];

	private Message() {
	}

	static Message send(int source, int s_piggyback, byte[] payload) {
		Message m = new Message();
		m.command = SEND;
		m.length = (short) payload.length;
		m.payload = payload;
		m.checksum = m.calculateChecksum(payload);
		m.source = source;
		m.messageID = s_piggyback;
		return m;
	}

	static Message ack(int destination,	int r_piggyback) {
		Message m = new Message();
		m.command = ACK;
		m.source = destination;
		m.messageID = r_piggyback;
		return m;

	}

	static Message miss(int destination, int r_piggyback) {
		Message m = new Message();
		m.command = MISS;
		m.source = destination;
		m.messageID = r_piggyback;
		return m;
	}

	static Message fromByte(byte[] bytes) throws ChecksumFailedException {

		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Message tmp = new Message();
		tmp.command = buffer.get();
		tmp.source = buffer.getInt();
		tmp.messageID = buffer.getInt();
		tmp.checksum = buffer.getLong();
		tmp.length = buffer.getShort();
		tmp.payload = (byte[]) Arrays.copyOfRange(buffer.array(), HEADER_SIZE,
				HEADER_SIZE + tmp.length);

		long cchecksum = tmp.calculateChecksum(tmp.payload);
		if (cchecksum != tmp.checksum) {
			throw new ChecksumFailedException();
		}

		return tmp;
	}

	byte[] toByte() {
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + length);
		buffer.put(command);
		buffer.putInt(source);
		buffer.putInt(messageID);
		buffer.putLong(checksum);
		buffer.putShort(length);
		buffer.put(payload);
		return buffer.array();
	}

	byte getCommand() {
		return command;
	}

	int getSource() {
		return source;
	}

	int getMessageID() {
		return messageID;
	}

	long getChecksum() {
		return checksum;
	}

	short getLength() {
		return length;
	}

	byte[] getPayload() {
		return payload;
	}

	private long calculateChecksum(byte[] data, int offset, int length) {
		CRC32 crc32 = new CRC32();
		crc32.update(data, offset, length);
		return crc32.getValue();
	}

	private long calculateChecksum(byte[] data) {
		return calculateChecksum(data, 0, data.length);
	}
	
	@Override
	public String toString() {
		return cmdToText(command) + " " +source + "(" + messageID + ")  => " + byteToText(payload);
	}
	
	private String byteToText(byte[] bytes) {
		StringBuffer bf = new StringBuffer();
		for (byte b : bytes) {
		bf.append((char)b);
		}
		return bf.toString();
	}
	
	private String cmdToText(byte command) {
		switch (command) {
		case ACK: return "ACK";
		case MISS: return "MISS";
		case SEND: return "SEND";
		default: return "STRANGE";
		}
	}
	
}