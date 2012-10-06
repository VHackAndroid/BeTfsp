package edu.vub.nfc.thing.listener;

import edu.vub.nfc.thing.Thing;

public interface ThingSavedListener<T extends Thing> {
	public void signal(T thing);
}
