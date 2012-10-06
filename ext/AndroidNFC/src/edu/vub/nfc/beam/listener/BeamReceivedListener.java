package edu.vub.nfc.beam.listener;

import edu.vub.nfc.NFCActivity;
import edu.vub.nfc.tag.NdefMessageToObjectConverter;
import edu.vub.nfc.thing.ThingAdapter;
import android.nfc.NdefMessage;

// TODO: filter on beam MIME type
public class BeamReceivedListener {
	private NFCActivity nfcActivity_;
	private NdefMessageToObjectConverter converter_;
	
	// Constructor takes an NFCActivity.
	public BeamReceivedListener(NFCActivity activity, NdefMessageToObjectConverter converter) {
		nfcActivity_ = activity;
		converter_ = converter;
		activity.addBeamListener(this);
	}
	
	// Cancel discoverer
	public void cancel() {
		nfcActivity_.removeBeamListener(this);
	}
	
	public void checkConditionAndNotify(NdefMessage message) {
		Object o = converter_.convert(message);
		if (checkCondition(o)) {
			onBeamReceived(o);
		}
	}
	
	// Optionally override to check condition on beamed object
	public boolean checkCondition(Object o) {
		return true;
	}
	
	// Override to react on beam
	public void onBeamReceived(Object o) { }
}
