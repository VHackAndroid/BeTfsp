package edu.vub.nfc.thing;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import edu.vub.nfc.tag.ObjectToNdefMessageConverter;

public class ThingToNdefMessageConverter implements ObjectToNdefMessageConverter {
	
	@Override
	public NdefMessage convert(Object o) {
		Thing thing = (Thing) o;
		NdefRecord wrapperRecord = ThingAdapter.createThingRecord(thing);
		return new NdefMessage(new NdefRecord[] { wrapperRecord });
	}

}
