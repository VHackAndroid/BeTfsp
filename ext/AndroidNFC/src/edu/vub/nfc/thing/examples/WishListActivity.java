package edu.vub.nfc.thing.examples;

import android.util.Log;
import edu.vub.nfc.thing.EmptyRecord;
import edu.vub.nfc.thing.Thing;
import edu.vub.nfc.thing.ThingActivity;
import edu.vub.nfc.thing.listener.ThingSavedListener;

public class WishListActivity extends ThingActivity<StringThing> {

	public void whenDiscovered(StringThing s) {
		Log.d(ThingActivity.LOG_TAG, "Discovered a string: " + s);
		s.setContent("new content");
		s.saveAsync();	// Ok + with callbacks
	}
	
	public void whenDiscovered(EmptyRecord r) {
		r.initialize(new StringThing(this, "test text"), new ThingSavedListener<StringThing>() {
			@Override
			public void signal(StringThing thing) {
				Log.v("THING", "Success!");
			}
		});
	}

	@Override
	public Class<? extends Thing> getThingType() {
		return StringThing.class;
	}
	
}
