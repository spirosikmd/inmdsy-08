package nl.rug.ds;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Message {

	private static final String ENCODING = "UTF-8";

	private static final int MAX_SIZE = 65508;
	//private static final int SOURCE_OFFSET = 4;
	//private static final int MESSAGE_OFFSET = 8;

	private int number;
	private int source;
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
		this.message = message;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSource(int source) {
		this.source = source;
	}
	
	public int getLength() {
		int msgLength = 0;
		try {
			msgLength = message.getBytes(ENCODING).length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return 8 + msgLength;
	}

	public static Message fromByte(byte[] bytes) {
		Message tmp = new Message();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		tmp.setNumber(buffer.getInt());
		tmp.setSource(buffer.getInt());
		tmp.setMessage(buffer.asCharBuffer().toString());
		return tmp;
	}

	public byte[] toByte() {
		
		ByteBuffer bytes = ByteBuffer.allocateDirect(0);
		try {
			byte[] messageBytes = message.getBytes(ENCODING);
			int length = messageBytes.length;
			System.out.println(length);
			//bytes.position(MESSAGE_OFFSET);
			
			bytes = ByteBuffer.allocate(getLength());
			bytes.putInt(number);
			bytes.putInt(source);
			bytes.put(messageBytes, 0, length);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes.array();
	}

}
