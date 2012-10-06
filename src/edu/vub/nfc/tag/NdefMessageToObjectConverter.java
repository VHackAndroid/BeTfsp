package edu.vub.nfc.tag;

import android.nfc.NdefMessage;

// Use this to convert NdefMessages to application-specific objects
public interface NdefMessageToObjectConverter extends TagDataConverter {
	public Object convert(NdefMessage ndefMessage);
}
