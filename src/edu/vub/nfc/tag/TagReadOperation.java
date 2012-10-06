package edu.vub.nfc.tag;

import edu.vub.nfc.tag.listener.TagReadFailedListener;
import edu.vub.nfc.tag.listener.TagReadListener;

public class TagReadOperation implements TagOperation {
	private TagReadListener successListener_;
	private TagReadFailedListener failureListener_;
	private long firstAttemptMillis_;
	private long timeout_;
	
	public TagReadOperation(TagReadListener success, TagReadFailedListener fail, long timeout) {
		successListener_ = success;
		failureListener_ = fail;
		timeout_ = timeout;
		firstAttemptMillis_ = System.currentTimeMillis();
	}
	
	@Override
	public TagReadListener getSuccessListener() {
		return successListener_;
	}
	
	@Override
	public TagReadFailedListener getFailureListener() {
		return failureListener_;
	}
	
	@Override
	public void setFirstAttemptMillis(long newMillis) {
		firstAttemptMillis_ = newMillis;
	}
	
	@Override
	public long getFirstAttemptMillis() {
		return firstAttemptMillis_;
	}
	
	@Override
	public boolean isTimedOut() {
		return (System.currentTimeMillis() - firstAttemptMillis_) > timeout_;
	}
	
	@Override
	public boolean isReadOperation() {
		return true;
	}

	@Override
	public boolean isWriteOperation() {
		return false;
	}
}
