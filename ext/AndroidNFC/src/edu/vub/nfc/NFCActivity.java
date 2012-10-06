package edu.vub.nfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import edu.vub.nfc.beam.Beamer;
import edu.vub.nfc.beam.listener.BeamFailedListener;
import edu.vub.nfc.beam.listener.BeamReceivedListener;
import edu.vub.nfc.beam.listener.BeamSuccessListener;
import edu.vub.nfc.exceptions.DifferentTagIdException;
import edu.vub.nfc.tag.ObjectToNdefMessageConverter;
import edu.vub.nfc.tag.TagDiscoverer;
import edu.vub.nfc.tag.TagReference;
import edu.vub.nfc.tag.TagReferenceFactory;

// Activity that notifies TagDiscoverers of detected NFC tags and offers some asynchronous abstractions for Android Beaming
public class NFCActivity extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	// This converter is only used for converting null messages.
	private ObjectToNdefMessageConverter nullMessageConverter_;
	
	private Vector<TagDiscoverer> tagDiscoverers_ = new Vector<TagDiscoverer>();
	private Vector<BeamReceivedListener> beamListeners_ = new Vector<BeamReceivedListener>();
	private Vector<NdefMessage> ndefMessagesToBeam_ = new Vector<NdefMessage>();
	private HashMap<NdefMessage, Beamer> ndefMessagesBeamers_ = new HashMap<NdefMessage, Beamer>();
	private HashMap<NdefMessage, BeamSuccessListener> ndefMessagesSuccessListeners_ = new HashMap<NdefMessage, BeamSuccessListener>();
	private HashMap<NdefMessage, BeamFailedListener> ndefMessagesFailedListeners_ = new HashMap<NdefMessage, BeamFailedListener>();
	NfcAdapter mNfcAdapter_;
	static final int NROFBEAMTHREADS = 4;
	
	private ScheduledThreadPoolExecutor beamThreadExecutor_ = new ScheduledThreadPoolExecutor(NROFBEAMTHREADS);
	private HashMap<NdefMessage, Runnable> ndefMessagesRunnables_ = new HashMap<NdefMessage, Runnable>();
	
	private HashSet<String> ndefDataTypesToFilterOn_ = new HashSet<String>();
	
	// This should normally not be called
	public void onCreate(Bundle savedInstanceState) {
		Log.e("NFCActivity", "onCreate called with a single argument, four arguments needed for NFCActivity!");
		super.onCreate(savedInstanceState);
	};
	
	// This is the constructor to use, write converter (for null messages) and tag mime-type are passed.
	// Converter is used for beaming null messages only. Tag References and Beamers encapsulate their own converters.
    public void onCreate(
    		Bundle savedInstanceState, 
    		ObjectToNdefMessageConverter objectToNdefMessageConverter) {
        super.onCreate(savedInstanceState);
        nullMessageConverter_ = objectToNdefMessageConverter;
        
        mNfcAdapter_ = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter_ == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter_.setNdefPushMessageCallback(this, this);
        mNfcAdapter_.setOnNdefPushCompleteCallback(this, this, new Activity[0]);
    }
    
    // Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages.
    public void beam(Beamer beamer, NdefMessage message) {
    	ndefMessagesToBeam_.add(message);
    	ndefMessagesBeamers_.put(message, beamer);
    }
    
    // Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages.
    // Register BeamSuccessListener which will be notified when the beam succeeds.
    public void beam(Beamer beamer, NdefMessage message, BeamSuccessListener listener) {
    	ndefMessagesToBeam_.add(message);
    	ndefMessagesBeamers_.put(message, beamer);
    	ndefMessagesSuccessListeners_.put(message, listener);
    }
    
    // Schedule asynchronous NdefMessage to be beamed to any device willing to accept NdefMessages for timeout timeout.
    // Register BeamSuccessListener which will be notified when the beam succeeds.
    // Register BeamFailedListener which will be notified when the beam did not succeed for timeout timeout.
    public void beam(Beamer beamer, final NdefMessage message, BeamSuccessListener successlistener, BeamFailedListener failedListener, long timeout) {
    	ndefMessagesToBeam_.add(message);
    	ndefMessagesBeamers_.put(message, beamer);
    	ndefMessagesSuccessListeners_.put(message, successlistener);
    	ndefMessagesFailedListeners_.put(message, failedListener);
    	final Activity activity = this;
    	Runnable runnable = new Runnable() {
			@Override
			public void run() {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						BeamFailedListener failedListener = ndefMessagesFailedListeners_.get(message);
						if (failedListener != null && (ndefMessagesToBeam_.contains(message))) {
							failedListener.signal();
							ndefMessagesToBeam_.remove(message);
							ndefMessagesBeamers_.remove(message);
							ndefMessagesSuccessListeners_.remove(message);
						}
					}
				});
			}
    	};
    	ndefMessagesRunnables_.put(message, runnable);	
    	beamThreadExecutor_.schedule(runnable, timeout, TimeUnit.MILLISECONDS);
    }
    
    // Adds a tag discoverer. Shouldn't be called by the programmer, is called from the TagDiscoverer constructor.
    public void addTagDiscoverer(TagDiscoverer discoverer) {
    	tagDiscoverers_.add(discoverer);
    }
    
   // Removes a tag discoverer. Shouldn't be called by the programmer, is called from the TagDiscoverer cancel method.
    public void removeTagDiscoverer(TagDiscoverer discoverer) {
    	byte[] tagType = discoverer.getTagType();
     	tagDiscoverers_.remove(discoverer);
     	boolean tagTypeStillNeeded = false;
     	Iterator<TagDiscoverer> it = tagDiscoverers_.iterator();
     	while (it.hasNext()) {
     		if (it.next().getTagType() == tagType) {
     			tagTypeStillNeeded = true;
     		}
     	}
     	if (!tagTypeStillNeeded) {
     		ndefDataTypesToFilterOn_.remove(new String(tagType, Charset.forName("UTF-8")));
     	}
    }
    
    // Adds a BeamListener. Shouldn't be called by the programmer, is called from the BeamListener constructor.
    public void addBeamListener(BeamReceivedListener listener) {
    	beamListeners_.add(listener);
    }
    
   // Removes a BeamListener. Shouldn't be called by the programmer, is called from the BeamListener cancel method.
    public void removeBeamListener(BeamReceivedListener listener) {
    	beamListeners_.remove(listener);
    }
    
    public void addNdefDataTypeToFilterOn(String dataType) {
    	ndefDataTypesToFilterOn_.add(dataType);
    }
    
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
    	if (ndefMessagesToBeam_.isEmpty()) {
    		return null;
    	} else {
    		return ndefMessagesToBeam_.firstElement();
    	}
    }
    
	@Override
	public void onNdefPushComplete(NfcEvent event) {
		if (ndefMessagesToBeam_.isEmpty()) {
			return;
		} else {
			NdefMessage message = ndefMessagesToBeam_.firstElement();
			ndefMessagesToBeam_.removeElementAt(0);
			ndefMessagesBeamers_.remove(message);
			final BeamSuccessListener successListener = ndefMessagesSuccessListeners_.get(message);
			if (successListener != null) {
				this.runOnUiThread(new Runnable() {
					public void run() {
						successListener.signal();
					}
				});
				ndefMessagesSuccessListeners_.remove(message);
			}
			ndefMessagesFailedListeners_.remove(message);
			beamThreadExecutor_.remove(ndefMessagesRunnables_.get(message));
		}
		if (ndefMessagesToBeam_.isEmpty()) {
			return;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		reactOnTagDetected(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mNfcAdapter_ = NfcAdapter.getDefaultAdapter(this);
		Intent intent = new Intent(this, getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter all = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        Iterator<String> it = ndefDataTypesToFilterOn_.iterator();
        String mime;
        while (it.hasNext()) {
        	try {
        		mime = it.next();
        		ndef.addDataType(mime);
        	} catch (MalformedMimeTypeException e) {
                //throw new RuntimeException("fail", e);
        		Log.e("ERROR", "Malformed MIME type passed to TagDiscoverer, skipping it...");
            }
        }
        IntentFilter[] mFilters = new IntentFilter[] { ndef, all };
        String[][] mTechLists = new String[][] { new String[] { Ndef.class.getName() }, new String[] { NdefFormatable.class.getName() } };
		
		mNfcAdapter_.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		
		Intent currentIntent = getIntent();
        reactOnTagDetected(currentIntent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
	}
	
	private void signalTagDetected(Intent intent, boolean emptyTag) {
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Ndef ndef;
		ndef = Ndef.get(tag);
		try {
			if (ndef == null) {
				NdefFormatable ndefForm = NdefFormatable.get(tag);
				if (ndefForm == null) {
					Log.d("ANDROIDNFC", "Couldn't create Ndef object, ignoring tag.");
					return;
				} else {
					ndefForm.connect();
					NdefRecord r = new NdefRecord(NdefRecord.TNF_EMPTY, new byte[] {}, new byte[] {}, new byte[] {});
					try {
						ndefForm.format(new NdefMessage(new NdefRecord[] {r}));
					} catch (FormatException e) {
						Log.d("ANDROIDNFC", "Couldn't format as Ndef, ignoring tag.");
						return;
					}
					ndefForm.close();
					ndef = Ndef.get(tag);
				}
			} else {
				ndef.connect();   // This will throw a NullpointerException if the received NdefMessage was beamed by another phone.
				ndef.close();
			}

			Log.d("ANDROIDNFC", "Signaling normal tag detection.");
			// First create the tagReference
			TagReference tagReference = TagReferenceFactory.newTagReference(this, tag, emptyTag);
			// Attempt to execute pending operations for that tag
			tagReference.processOperations();
			// Trigger all TagDiscoverers if their conditions match the tag
			// (may cause the scheduling of additional tag operations).
			Iterator<TagDiscoverer> it = tagDiscoverers_.iterator();
			while (it.hasNext()) {
				it.next().checkConditionSetConvertersAndNotify(tagReference);
			}
			Log.d("ANDROIDNFC", "Done processing tag.");
		} catch (NullPointerException e) {
			Log.d("ANDROIDNFC", "Signaling beam message reception.");
			Iterator<BeamReceivedListener> it = beamListeners_.iterator();
			while (it.hasNext()) {
				it.next().checkConditionAndNotify(ndef.getCachedNdefMessage());
			}
		} catch (IOException e) {
			Log.e("ERROR", "Tag could not be read.", e);
		} catch (DifferentTagIdException e) {
			Log.e("ERROR", "Attempt at updating TagReference's internal Tag object with Tag object with different ID.");
		} finally {
			try {
				ndef.close();
			} catch(Exception e) {
				Log.v("ANDROIDNFC", Log.getStackTraceString(e));
			}
		}
	}
	
	private void reactOnTagDetected(Intent intent) {
	    // Non-Ndef-formatted tag, first format as such.
	    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
	    	signalTagDetected(intent, true);
	    	return;
	    }
	    // Ndef-formatted tag, ready to use.
	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
	    	signalTagDetected(intent, false);
	    	return;
	    }
	}

}
