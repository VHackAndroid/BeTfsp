package edu.vub.nfc.tag;

import edu.vub.nfc.tag.listener.TagInteractionListener;

public interface TagOperation {
	public TagInteractionListener getSuccessListener();
	public TagInteractionListener getFailureListener();
	public void setFirstAttemptMillis(long millis);
	public long getFirstAttemptMillis();
	public boolean isTimedOut();
	public boolean isReadOperation();
	public boolean isWriteOperation();
}
