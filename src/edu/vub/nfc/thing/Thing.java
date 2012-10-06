package edu.vub.nfc.thing;

import android.util.Log;
import edu.vub.nfc.beam.Beamer;
import edu.vub.nfc.beam.listener.BeamFailedListener;
import edu.vub.nfc.beam.listener.BeamSuccessListener;
import edu.vub.nfc.tag.TagReference;
import edu.vub.nfc.tag.listener.TagWriteFailedListener;
import edu.vub.nfc.tag.listener.TagWrittenListener;
import edu.vub.nfc.thing.listener.ThingBroadcastFailedListener;
import edu.vub.nfc.thing.listener.ThingBroadcastSuccessListener;
import edu.vub.nfc.thing.listener.ThingSaveFailedListener;
import edu.vub.nfc.thing.listener.ThingSavedListener;

public class Thing implements ThingInterface {

	private static final int BEAM_TIMEOUT = 3000;
	private transient TagReference tagReference_;
	private transient ThingActivity activity_;
	
	private Thing() {
		Log.e("THING", "Usage of default exception is not allowed, need an activity!");
	}
	
	public Thing(ThingActivity activity) {
		activity_ = activity;
	}
	
	public boolean initializeTag(final EmptyRecord record, final ThingSavedListener successListener) {
		if (tagReference_ != null) {
			Log.d("THING", "Tag is already initialized");
			return false;
		}
		tagReference_ = record.getTagReference();
		saveAsync(successListener);
		return true;
	}
	
	public boolean initializeTag(final EmptyRecord record, final ThingSavedListener successListener, final ThingSaveFailedListener failedListener) {
		if (tagReference_ != null) {
			Log.d("THING", "Tag is already initialized");
			return false;
		}
		tagReference_ = record.getTagReference();
		saveAsync(successListener, failedListener);
		return true;
	}
	
	public boolean initializeTag(final EmptyRecord record, final ThingSavedListener successListener, final ThingSaveFailedListener failedListener, long timeout) {
		if (tagReference_ != null) {
			Log.d("THING", "Tag is already initialized");
			return false;
		}
		tagReference_ = record.getTagReference();
		saveAsync(successListener, failedListener, timeout);
		return true;
	}
	
	@Override
	public boolean isAssociated() {
		return (tagReference_ != null);
	}
	
	@Override
	public void saveAsync() {
		tagReference_.write(this);
	}
	
	@Override
	public void saveAsync(final ThingSavedListener successListener) {
		tagReference_.write(
				this, 
				new TagWrittenListener() {
					@Override
					public void signal(TagReference tagReference) {
						successListener.signal((Thing)tagReference.getCachedData());
					}
				});
	}
	
	@Override
	public void saveAsync(final ThingSavedListener successListener, final ThingSaveFailedListener failedListener) {
		tagReference_.write(
				this, 
				new TagWrittenListener() {
					@Override
					public void signal(TagReference tagReference) {
						successListener.signal((Thing)tagReference.getCachedData());
					}
				}, 
				new TagWriteFailedListener() {
					@Override
					public void signal(TagReference tagReference) {
						failedListener.signal();
					}
				}, 
				3000);
	}
	
	@Override
	public void saveAsync(final ThingSavedListener successListener, final ThingSaveFailedListener failedListener, long timeout) {
		tagReference_.write(
				this, 
				new TagWrittenListener() {
					@Override
					public void signal(TagReference tagReference) {
						successListener.signal((Thing)tagReference.getCachedData());
					}
				}, 
				new TagWriteFailedListener() {
					@Override
					public void signal(TagReference tagReference) {
						failedListener.signal();
					}
				}, 
				timeout);
	}
	
	public void setTagReference(TagReference tagReference) {
		tagReference_ = tagReference;
	}

	@Override
	public void broadcast() {
		//NFCActivity activity = (NFCActivity) tagReference_.getActivity();
		ThingToNdefMessageConverter converter = new ThingToNdefMessageConverter();
		(new Beamer(activity_, converter)).beam(this);
	}

	@Override
	public void broadcast(final ThingBroadcastSuccessListener successListener) {
		//NFCActivity activity = (NFCActivity) tagReference_.getActivity();
		ThingToNdefMessageConverter converter = new ThingToNdefMessageConverter();
		final Thing theThing = this;
		Beamer b = new Beamer(activity_, converter);
		b.beam(this,
				new BeamSuccessListener() {
			@Override
			public void signal() {
				successListener.signal(theThing);
			}
		});
	}

	@Override
	public void broadcast(final ThingBroadcastSuccessListener successListener,
			final ThingBroadcastFailedListener failedListener) {
		this.broadcast(successListener, failedListener, BEAM_TIMEOUT);
	}

	@Override
	public void broadcast(final ThingBroadcastSuccessListener successListener,
			final ThingBroadcastFailedListener failedListener, long timeout) {
		//NFCActivity activity = (NFCActivity) tagReference_.getActivity();
		ThingToNdefMessageConverter converter = new ThingToNdefMessageConverter();
		final Thing theThing = this;
		Beamer b = new Beamer(activity_, converter);
		b.beam(this,
				new BeamSuccessListener() {
			@Override
			public void signal() {
				successListener.signal(theThing);
			}
		},
		new BeamFailedListener() {
			@Override
			public void signal() {
				failedListener.signal(theThing);
			}
		},
		timeout);
	}
}
