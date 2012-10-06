package edu.vub.nfc.tag;

import android.app.Activity;
import android.nfc.Tag;
import edu.vub.nfc.NFCActivity;
import edu.vub.nfc.exceptions.DifferentTagIdException;
import edu.vub.nfc.tag.listener.TagReadFailedListener;
import edu.vub.nfc.tag.listener.TagReadListener;
import edu.vub.nfc.tag.listener.TagWriteFailedListener;
import edu.vub.nfc.tag.listener.TagWrittenListener;

// TagReference interface that encapsulates concrete TagReference implementations.
public interface TagReference {
	
	// Get the tag ID
	public byte[] getTagId();

	// Get the cached data from the tag.
	public Object getCachedData();
	
	// Was this tag detected already before?
	public boolean isNewTag();
	
	// Asynchronously read the data on the tag in a separate thread.
	// If it not immediately succeeds, it keeps attempting.
	// Takes a listener that is notified when the read succeeds.
	public void read(TagReadListener listener);
	
	// Asynchronously read the data on the tag in a separate thread.
	// If it not immediately succeeds, it keeps attempting, until timeout (in milliseconds) is passed.
	// Takes a listener that is notified when the read succeeds and a listener that is notified when the read fails.
	public void read(TagReadListener successListener, TagReadFailedListener failedListener, long timeout);
	
	// Asynchronously write the data on the tag in a separate thread.
	// If it not immediately succeeds, it keeps attempting.
	public void write(Object toWrite);
	
	// Asynchronously write the data on the tag in a separate thread.
	// If it not immediately succeeds, it keeps attempting.
	// Takes a listener that is notified when the write succeeds.
	public void write(Object toWrite, TagWrittenListener listener);
	
	// Asynchronously write the data on the tag in a separate thread.
	// If it not immediately succeeds, it keeps attempting.
	// Takes a listener that is notified when the write succeeds and a listener that is notified when the write fails.
	public void write(Object toWrite, TagWrittenListener successListener, TagWriteFailedListener failedListener, long timeout);
	
	// Makes the TagReference process its list of pending operations (if possible).
	public void processOperations();
	
	// Converters must be set by the TagDiscoverer when it creates/retrieves a TagReference.
	public void setReadConverter(NdefMessageToObjectConverter readConverter);
	public void setWriteConverter(ObjectToNdefMessageConverter writeConverter);
	
	// Set the internal tag object (this is because the Android NFC API is ugly and generates new Tag
	// objects for each tag detected, even if this tag was seen before and a Tag object already exists).
	public void setTag(Tag tag) throws DifferentTagIdException;
	
	public boolean isEmpty();
	
	public Activity getActivity();

}
