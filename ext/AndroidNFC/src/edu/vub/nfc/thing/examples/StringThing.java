package edu.vub.nfc.thing.examples;

import edu.vub.nfc.thing.Thing;
import edu.vub.nfc.thing.ThingActivity;


public class StringThing extends Thing {
	
	private String content_;

	public StringThing(ThingActivity<StringThing> activity, String content) {
		super(activity);
		content_ = content;
	}
	
	public String getContent() {
		return content_;
	}
	
	public void setContent(String content) {
		this.content_ = content;
	}
	
	public String toString() {
		return content_;
	}

}
