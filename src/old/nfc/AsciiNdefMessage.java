package old.nfc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AsciiNdefMessage {

	public static final byte[] ASCII_TAG =
	        new byte[] {(byte) 0xd1, (byte) 0x01, (byte) 0x1c, (byte) 0x54, (byte) 0x02, (byte) 0x65, (byte) 0x6e};
	
    /**
     * A plain text tag in ASCII.
     */
    public static byte[] CreateNdefMessage(String text) {
    	ByteArrayOutputStream msg = new ByteArrayOutputStream();
    	try {
        	msg.write(ASCII_TAG);
			msg.write(text.getBytes("ASCII"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return msg.toByteArray();
    }
}
