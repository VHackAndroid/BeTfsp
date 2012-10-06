package edu.vub.nfc.beam;

import android.nfc.NdefMessage;
import android.util.Log;
import edu.vub.nfc.NFCActivity;
import edu.vub.nfc.beam.listener.BeamFailedListener;
import edu.vub.nfc.beam.listener.BeamSuccessListener;
import edu.vub.nfc.tag.ObjectToNdefMessageConverter;

// Beamer object used for asynchronously beaming objects.
public class Beamer {
	private ObjectToNdefMessageConverter objectToNdefMessageConverter_;
	private NFCActivity activity_;
	
	public Beamer(NFCActivity activity, ObjectToNdefMessageConverter converter) {
		activity_ = activity;
		objectToNdefMessageConverter_ = converter;
	}
	
	// Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages.
	public void beam(Object o) {
		activity_.beam(this, objectToNdefMessageConverter_.convert(o));
    }
    
    // Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages.
    // Register BeamSuccessListener which will be notified when the beam succeeds.
    public void beam(Object o, BeamSuccessListener listener) {
    	NdefMessage message = objectToNdefMessageConverter_.convert(o);
    	activity_.beam(this, message, listener);
    }
    
    // Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages for timeout timeout.
    // Register BeamSuccessListener which will be notified when the beam succeeds.
    // Register BeamFailedListener which will be notified when the beam did not succeed for timeout timeout.
    public void beam(Object o, BeamSuccessListener successlistener, BeamFailedListener failedListener, long timeout) {
    	final NdefMessage message = objectToNdefMessageConverter_.convert(o);
    	activity_.beam(this, message, successlistener, failedListener, timeout);
    }
}
