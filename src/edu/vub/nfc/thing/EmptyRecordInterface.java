package edu.vub.nfc.thing;

import edu.vub.nfc.thing.listener.ThingSaveFailedListener;
import edu.vub.nfc.thing.listener.ThingSavedListener;


public interface EmptyRecordInterface {
	
	public void initialize(Thing o, ThingSavedListener successListener);
	
	public void initialize(Thing o, ThingSavedListener successListener, ThingSaveFailedListener failedListener);
	
	public void initialize(Thing o, ThingSavedListener successListener, ThingSaveFailedListener failedListener, long timeout);
	
	public boolean isInitialized();
}
