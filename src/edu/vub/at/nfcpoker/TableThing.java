package edu.vub.at.nfcpoker;

import edu.vub.nfc.thing.Thing;
import edu.vub.nfc.thing.ThingActivity;

public class TableThing extends Thing {
	
	public String ip_;
	public int port_;
	
	public TableThing(ThingActivity<TableThing> activity, String ip, int port) {
		super(activity);
		ip_ = ip;
		port_ = port; 
	}

}
