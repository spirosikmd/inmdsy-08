package nl.rug.ds;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Uses a CRC32 checksum check for integrity 
 * 
 * @author cm
 *
 */
public class Message {

	private static final int HEADER_LENGTH = 20;

	private static final String ENCODING = "UTF-8";
	
	private int number; //check for integrity (reject duplicates), piggyback
	private int source;
	private int payloadLength = 0;
	private String message="";

	public String getMessage() {
		return message;
	}

	public int getNumber() {
		return number;
	}

	public int getSource() {
		return source;
	}

	public void setMessage(String message) {
		if (message == null) {
			message = "";
		}
		try {
			payloadLength = message.getBytes(ENCODING).length;			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.message = message;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSource(int source) {
		this.source = source;
	}
	
	public int getLength() {	
		return HEADER_LENGTH + payloadLength;
	}

	public static Message fromByte(byte[] bytes) throws ChecksumFailedException {
		Message tmp = new Message();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		long checksum = buffer.getLong();		
		tmp.setNumber(buffer.getInt());
		tmp.setSource(buffer.getInt());
		int msgLength = buffer.getInt();
		
		try {
			tmp.setMessage(new String(buffer.array(), HEADER_LENGTH, msgLength, ENCODING));
			CRC32 crc32 = new CRC32();
			byte[] messageBytes = tmp.getMessage().getBytes(ENCODING);
			crc32.update(messageBytes);

			if (checksum != crc32.getValue()) {
				throw new ChecksumFailedException();
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return tmp;
	}

	public byte[] toByte() {
		
		ByteBuffer bytes = ByteBuffer.allocateDirect(0);
		try {
			byte[] messageBytes = message.getBytes(ENCODING);
			CRC32 crc32 = new CRC32();
			crc32.update(messageBytes);

			long checksum = crc32.getValue();
			
			bytes = ByteBuffer.allocate(getLength());
			
			bytes.putLong(checksum);
			bytes.putInt(number);
			bytes.putInt(source);
			bytes.putInt(payloadLength);
			bytes.put(messageBytes, 0, payloadLength);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bytes.array();
	}
	
}
