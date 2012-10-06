package edu.vub.nfc.thing;

import java.nio.charset.Charset;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.google.gson.Gson;

public class ThingAdapter {
	
	public static final String THING_MIME = "x-thing/";
	public static final String THING_MIME_WILDCARD = THING_MIME + "*";
	
	public static final String THING_MIME_READONLY = "x-thing-readonly/";
	public static final String THING_MIME_READONLY_WILDCARD = THING_MIME + "*";
	
	private static final Gson GSON = new Gson();
			
	public static String createThingMimeType(Class c) {
		return createThingMimeType(c.getName());
	}
	
	public static String createThingMimeType(String className) {
		//return (THING_MIME + className).replaceAll("\\.", "-");
		return (THING_MIME + className);
	}
	
	public static String getThingMime(NdefRecord record) {
		return new String(record.getType());
	}
	
	public static String getThingClassName(String mime) {
		//return mime.replace(THING_MIME, "").replaceAll("-", ".");
		return mime.replace(THING_MIME, "");
	}
	
	public static String recordTypeToClassName(NdefRecord record) {
		return getThingClassName(getThingMime(record));
	}
	
	public static NdefRecord createThingRecord(Thing thing, Gson gson) {
		Class thingClass = thing.getClass();
		return new NdefRecord(
				NdefRecord.TNF_MIME_MEDIA, 
				toBytes(createThingMimeType(thingClass.getName())),
				new byte[] {},
				ThingAdapter.toBytes(gson.toJson(thing)));
	}
	
	public static NdefRecord createThingRecord(Thing thing) {
		return createThingRecord(thing, GSON);
	}
	
	public static boolean isThingMessage(NdefMessage msg) {
		NdefRecord[] records = msg.getRecords();
		String type;
		for (NdefRecord r : records) {
			type = new String(r.getType());
			if (type.startsWith(THING_MIME)) {
				return true;
			}
		}
		return false;
	}
		
	private static byte[] toBytes(String payload, Charset charset) {
		return payload.getBytes(charset);
	}
	
	public static byte[] toBytes(String payload) {
		return toBytes(payload, Charset.forName("UTF-8"));
	}

}
