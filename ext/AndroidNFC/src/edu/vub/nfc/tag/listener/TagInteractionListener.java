package edu.vub.nfc.tag.listener;

import edu.vub.nfc.tag.TagReference;

// Signal that the observed interaction occurred
public interface TagInteractionListener {
	public void signal(TagReference tagReference);
}
