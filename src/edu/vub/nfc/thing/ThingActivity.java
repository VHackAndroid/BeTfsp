package edu.vub.nfc.thing;

import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.util.Log;
import edu.vub.nfc.NFCActivity;
import edu.vub.nfc.beam.listener.BeamReceivedListener;
import edu.vub.nfc.tag.NdefMessageToObjectConverter;
import edu.vub.nfc.tag.ObjectToNdefMessageConverter;
import edu.vub.nfc.tag.TagDiscoverer;
import edu.vub.nfc.tag.TagReference;
import edu.vub.nfc.tag.listener.TagReadFailedListener;
import edu.vub.nfc.tag.listener.TagReadListener;
import edu.vub.nfc.thing.listener.ThingListenerInterface;

public abstract class ThingActivity<T extends Thing> extends NFCActivity implements ThingListenerInterface<T> {
	
	public static final long WHENDISCOVERED_TIMEOUT = 2000;
	public static final String LOG_TAG = "DISTOBJ";
	private ThingDiscoverer discoverer_;
	
	/*
	 * subclasses must define which type of thing they wish to discover
	 */
	public abstract Class<? extends Thing> getThingType();
	
	private String getMimeType() {
		return ThingAdapter.createThingMimeType(getThingType());
	}
    
    private class ThingDiscoverer extends TagDiscoverer {
		
		public ThingDiscoverer(NFCActivity activity, String tagType, NdefMessageToObjectConverter readConverter, ObjectToNdefMessageConverter writeConverter) {
			super(activity, ThingAdapter.toBytes(getMimeType()), readConverter, writeConverter);
		}
		
		public void onEmptyTagDetected(TagReference reference) {
			whenDiscovered(new EmptyRecord(reference));
		}
		
		@Override
		public void onTagDetected(TagReference tagReference) {
			if (tagReference.isEmpty()) {
				whenDiscovered(new EmptyRecord(tagReference));
			} else {
				tagReference.read(
						new TagReadListener() {
							@Override
							public void signal(TagReference tagReference) {
								T thing = (T) tagReference.getCachedData();
								thing.setTagReference(tagReference);
								whenDiscovered(thing);	
							}
						}, 
						new TagReadFailedListener() {
							@Override
							public void signal(TagReference tagReference) {
								Log.d("ANDROIDNFC", "Tag read timed out during whenDiscovered");
							}
						},
						WHENDISCOVERED_TIMEOUT);
			}
		}
		
		@Override
		public void onTagRedetected(TagReference tagReference) {
			tagReference.read(
					new TagReadListener() {
						@Override
						public void signal(TagReference tagReference) {
							T thing = (T) tagReference.getCachedData();
							thing.setTagReference(tagReference);
							whenDiscovered(thing);
							whenRediscovered(thing);
						}
					}, 
					new TagReadFailedListener() {
						@Override
						public void signal(TagReference tagReference) {
							Log.d("ANDROIDNFC", "Tag read timed out during whenDiscovered");
						}
					},
					WHENDISCOVERED_TIMEOUT);
		}
	}
    
    private class ThingBeamListener extends BeamReceivedListener {
		
		public ThingBeamListener(NFCActivity activity, NdefMessageToObjectConverter converter) {
			super(activity, converter);
		}
		
		@Override
		public void checkConditionAndNotify(NdefMessage message) {
			if (ThingAdapter.isThingMessage(message)) {
				super.checkConditionAndNotify(message);
			}
		}
		
		@Override
		public void onBeamReceived(Object o) {
			whenDiscovered((T)o);
		}
	}
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState, new ThingToNdefMessageConverter());
		if (isNFCSupported()) {
			this.discoverer_ = new ThingDiscoverer(this, getMimeType(), new NdefMessageToThingConverter(), new ThingToNdefMessageConverter());
			new ThingBeamListener(this, new NdefMessageToThingConverter());
		}
	}	
	
	// to be implemented by subclasses
	public  void whenDiscovered(T thing) {};
	
	public void whenDiscovered(EmptyRecord r) {};
	
	public void whenRediscovered(T thing) {};

}
