package edu.vub.nfc.thing;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.google.gson.Gson;

import edu.vub.nfc.tag.NdefMessageToObjectConverter;

public class NdefMessageToThingConverter<T extends Thing> implements NdefMessageToObjectConverter {
	
	private final Gson gson_ = new Gson();

	@Override
	public T convert(NdefMessage ndefMessage) {
		if (ThingAdapter.isThingMessage(ndefMessage)) {
			NdefRecord[] records = ndefMessage.getRecords();
			NdefRecord thingRecord = records[0];
			String className = ThingAdapter.recordTypeToClassName(thingRecord);
			Class thingClass;
			try {
				thingClass = Class.forName(className);
				return (T) gson_.fromJson(new String(thingRecord.getPayload()), thingClass);
			} catch (ClassNotFoundException e) {
				Log.e("ANDROID NFC ERROR", Log.getStackTraceString(e));
				return null;
			}
		} else {
			return null;
		}
	}

}
