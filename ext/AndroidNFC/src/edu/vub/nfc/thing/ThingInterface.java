package edu.vub.nfc.thing;

import edu.vub.nfc.thing.listener.ThingBroadcastFailedListener;
import edu.vub.nfc.thing.listener.ThingBroadcastSuccessListener;
import edu.vub.nfc.thing.listener.ThingSaveFailedListener;
import edu.vub.nfc.thing.listener.ThingSavedListener;

public interface ThingInterface {
	
	public boolean isAssociated();
	
	public void saveAsync();

	public void saveAsync(ThingSavedListener successListener);

	public void saveAsync(ThingSavedListener successListener, ThingSaveFailedListener failedListener);

	public void saveAsync(ThingSavedListener successListener, ThingSaveFailedListener failedListener, long timeout);
	
	public void broadcast();
	
	public void broadcast(ThingBroadcastSuccessListener successListener);
	
	public void broadcast(ThingBroadcastSuccessListener successListener, ThingBroadcastFailedListener failedListener);
	
	public void broadcast(ThingBroadcastSuccessListener successListener, ThingBroadcastFailedListener failedListener, long timeout);
	
}
