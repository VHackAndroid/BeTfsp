package edu.vub.nfc.tag;

import edu.vub.nfc.tag.listener.TagWriteFailedListener;
import edu.vub.nfc.tag.listener.TagWrittenListener;



public class TagWriteOperation implements TagOperation {
	private TagWrittenListener successListener_;
	private TagWriteFailedListener failureListener_;
	private long firstAttemptMillis_;
	private long timeout_;
	private Object toWrite_;
	
	public TagWriteOperation(TagWrittenListener success, TagWriteFailedListener fail, long timeout, Object toWrite) {
		successListener_ = success;
		failureListener_ = fail;
		timeout_ = timeout;
		toWrite_ = toWrite;
		firstAttemptMillis_ = System.currentTimeMillis();
	}
	
	@Override
	public TagWrittenListener getSuccessListener() {
		return successListener_;
	}

	@Override
	public TagWriteFailedListener getFailureListener() {
		return failureListener_;
	}

	@Override
	public void setFirstAttemptMillis(long millis) {
		firstAttemptMillis_ = millis;
	}

	@Override
	public long getFirstAttemptMillis() {
		return firstAttemptMillis_;
	}

	@Override
	public boolean isTimedOut() {
		return (System.currentTimeMillis() - firstAttemptMillis_) > timeout_;
	}
	
	public Object getObjectToWrite() {
		return toWrite_;
	}
	
	@Override
	public boolean isReadOperation() {
		return false;
	}

	@Override
	public boolean isWriteOperation() {
		return true;
	}

}
