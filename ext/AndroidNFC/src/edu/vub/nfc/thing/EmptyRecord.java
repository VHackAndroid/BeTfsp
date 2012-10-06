package edu.vub.nfc.thing;

import edu.vub.nfc.tag.TagReference;
import edu.vub.nfc.thing.listener.ThingSaveFailedListener;
import edu.vub.nfc.thing.listener.ThingSavedListener;

public class EmptyRecord implements EmptyRecordInterface {
	
	public static final int RECORD_INITIALIZED = 0;
	private transient TagReference tagReference_;
	private transient boolean isInitialized_ = false;

	public EmptyRecord(TagReference tagReference) {
		tagReference_ = tagReference;
	}

	@Override
	public void initialize(Thing o, ThingSavedListener successListener) {
		o.initializeTag(this, successListener);
	}

	@Override
	public void initialize(Thing o, ThingSavedListener successListener,
			ThingSaveFailedListener failedListener) {
		o.initializeTag(this, successListener, failedListener);
	}

	@Override
	public void initialize(Thing o, ThingSavedListener successListener,
			ThingSaveFailedListener failedListener, long timeout) {
		o.initializeTag(this, successListener, failedListener, timeout);
	}
	
	public TagReference getTagReference() {
		return tagReference_;
	}

	@Override
	public boolean isInitialized() {
		return isInitialized_;
	}
	
	public void setInitialized() {
		isInitialized_ = true;
	}
}
