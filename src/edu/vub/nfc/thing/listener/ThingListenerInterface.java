package edu.vub.nfc.thing.listener;

import edu.vub.nfc.thing.EmptyRecord;
import edu.vub.nfc.thing.Thing;

public interface ThingListenerInterface<T extends Thing> {
	
	public void whenDiscovered(T thing);
	
	public void whenDiscovered(EmptyRecord r);
	
	public void whenRediscovered(T thing);

}
