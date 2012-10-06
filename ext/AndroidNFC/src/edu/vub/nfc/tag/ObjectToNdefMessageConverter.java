package edu.vub.nfc.tag;

import android.nfc.NdefMessage;

//Use this to convert application-specific objects to NdefMessages
public interface ObjectToNdefMessageConverter extends TagDataConverter {
	public NdefMessage convert(Object o);
}
