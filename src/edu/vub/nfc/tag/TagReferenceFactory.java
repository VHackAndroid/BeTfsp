package edu.vub.nfc.tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gson.Gson;

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;
import edu.vub.nfc.NFCActivity;
import edu.vub.nfc.exceptions.DifferentTagIdException;
import edu.vub.nfc.tag.listener.TagReadFailedListener;
import edu.vub.nfc.tag.listener.TagReadListener;
import edu.vub.nfc.tag.listener.TagWriteFailedListener;
import edu.vub.nfc.tag.listener.TagWrittenListener;

// TagReferenceFactory should always be used to create TagReferences.
public class TagReferenceFactory {
	
	private static HashMap<byte[], ConcreteTagReference> tagIdReferenceMap_ = new HashMap<byte[], ConcreteTagReference>();
	

	// Create a new concrete TagReference
	// Takes the activity (for the UI thread), the tag and converters for NdefMessage<->Object conversions.
	// Only a single TagReference per tag should exist to prevent race conditions.
	public static ConcreteTagReference newTagReference(Activity activity, Tag tag, boolean emptyTag) throws DifferentTagIdException {
		ConcreteTagReference reference;
		if (tagIdReferenceMap_.containsKey(tag.getId())) {
			reference = tagIdReferenceMap_.get(tag.getId());
			// I already saw this tag.
			reference.isNewTag_ = false;
			// Must update internal Tag object: Android generates new Tag objects
			// when tags are scanned (even if already a Tag object exists) and old ones cannot be used for writing.
			reference.setTag(tag);
		} else {
			reference = new ConcreteTagReference(activity, tag, emptyTag);
			tagIdReferenceMap_.put(tag.getId(), reference);
		}
		return reference;
	}
	
	// Discards the TagReference (TagReferences are not automatically garbage collected!)
	public static boolean discardTagReference(Tag tag) {
		return tagIdReferenceMap_.remove(tag.getId()) != null;
	}
	
	
	// Concrete TagReference implementation.
	private static class ConcreteTagReference extends Thread implements TagReference {
		
		private String previousCachedDataSerialized_;
		private static final Gson GSON = new Gson(); 
		
		private Object cachedData_;
		private Tag tag_;
		private NdefMessageToObjectConverter ndefMessageToObjectConverter_;
		private ObjectToNdefMessageConverter objectToNdefMessageConverter_;
		private Activity activity_;
		private LinkedList<TagOperation> operationQueue_ = new LinkedList<TagOperation>();
		private long maxTimeout_ = 1000000;
		private boolean isNewTag_ = true;
		private boolean isEmpty_ = true;
		private boolean isRunning_ = false;
		private boolean stop_ = false;
		
		private ConcreteTagReference(Activity activity, Tag tag, boolean emptyTag) {
			tag_ = tag;
			activity_ = activity;
			isEmpty_ = emptyTag;
		}
		
		@Override
		public void run() {
			if (isRunning_) {
				return;
			} 
			stop_ = false;
			while (!operationQueue_.isEmpty()) {
				if (stop_) {
					isRunning_ = false;
					return;
				}
				processNextOperation();
			}
		}
		
		public void stopProcessing() {
			stop_ = true;
		}
		
		public void processOperations() {
			//printContentsOfOperationQueue();
			run();
		}
		
		private void printContentsOfOperationQueue() {
			Log.d("ANDROIDNFC", " ---- Printing contents of operation queue ----");
			Iterator<TagOperation> it = operationQueue_.iterator();
			while (it.hasNext()) {
				TagOperation operation = it.next();
				Log.d("ANDROIDNFC", "Operation: " +  operation.hashCode() + " write?: " + operation.isWriteOperation());
			}
			Log.d("ANDROIDNFC", " ---- Done ----");
		}
		
		public void processNextOperation() {
			TagOperation operation = operationQueue_.peek();
			doNdefClosedSanityCheck();
			if (operation.isReadOperation()) {
				if (processReadOperation(operation)) {
					operationQueue_.poll();
				}
			} else {
				if (processWriteOperation(operation)) {
					operationQueue_.poll();
				}
			}
		}
		
		
		public byte[] getTagId() {
			return tag_.getId();
		}
		
		public Object getCachedData() {
			return cachedData_;
		}
		
		public boolean isNewTag() {
			return isNewTag_;
		}
		
		public boolean isEmpty() {
			return isEmpty_;
		}
		
		public Activity getActivity() {
			return activity_;
		}
		
		private void doNdefClosedSanityCheck() {
			// Just a sanity check to be sure the Tag technology is closed
			try {
				Ndef ndef = Ndef.get(tag_);
				if (ndef != null) { 
					ndef.close();
				}
			} catch(Exception e) {
				Log.e("ANDROIDNFC", Log.getStackTraceString(e));
				Log.v("TagReference", "Nothing to see here, move along.");
			}
		}
		
		
		private void updateCachedData(NdefMessage message) {
			previousCachedDataSerialized_ = GSON.toJson(cachedData_);
			cachedData_ = ndefMessageToObjectConverter_.convert(message);
		}
		
		private void updateCachedData(Object newData) {
			previousCachedDataSerialized_ = GSON.toJson(cachedData_);
			cachedData_ = newData;
		}

		
		private boolean processReadOperation(final TagOperation operation) {
			final TagReference reference = this;
			if (operation.isTimedOut()) {
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						if (operation.isTimedOut()) {
							if (operation.getFailureListener() != null) {
								operation.getFailureListener().signal(reference);
							}
						}
					}
				}); 
				return true;
			}
			try {
				Ndef ndef = Ndef.get(tag_);
				ndef.connect();
				NdefMessage message = ndef.getNdefMessage();
				ndef.close();
				updateCachedData(message);
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						operation.getSuccessListener().signal(reference);
					}
				}); 
				return true;
			} catch(final FormatException e) {
				doNdefClosedSanityCheck();
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						Log.e("BADFORMAT", "Tag wrongly formatted!");
						if (operation.getFailureListener() != null) {
							operation.getFailureListener().signal(reference);
						}
					}						
				});
				return true;
			} catch(Exception e) {
				doNdefClosedSanityCheck();
				Log.e("DEBUG", e.toString());
				return false;
			}
		};
		
		private boolean processWriteOperation(final TagOperation operation) {
			final TagReference reference = this;
			if (operation.isTimedOut()) {
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						if (operation.isTimedOut()) {
							if (operation.getFailureListener() != null) {
								operation.getFailureListener().signal(reference);
							}
						}
					}
				}); 
				return true;
			}
			try {
				// First, try if the tag is an NDEF tag.
				Ndef ndef = Ndef.get(tag_);
				Object objectToWrite = ((TagWriteOperation)operation).getObjectToWrite();
				NdefMessage converted = objectToNdefMessageConverter_.convert(objectToWrite);
				if (ndef == null) {
					NdefFormatable ndefFormatable = NdefFormatable.get(tag_);
					ndefFormatable.connect();
					ndefFormatable.format(converted);
					ndefFormatable.close();
				} else {
					if (ndef.getMaxSize() < converted.toByteArray().length) {
						//doNdefClosedSanityCheck();
						activity_.runOnUiThread(new Runnable() {
							public void run() {
								Log.e("ANDROIDNFC", "Data too large to fit in tag memory!");
								if (operation.getFailureListener() != null) {
									operation.getFailureListener().signal(reference);
								}
							}
						});
						return true;
					} else {
						ndef.connect();
						ndef.writeNdefMessage(converted);
						ndef.close();
					};
				}
				updateCachedData(objectToWrite);
				isEmpty_ = false;
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						if (operation.getSuccessListener() != null) {
							operation.getSuccessListener().signal(reference);
						}
					}
				});
				return true;
			} catch(FormatException e) {
				doNdefClosedSanityCheck();
				activity_.runOnUiThread(new Runnable() {
					public void run() {
						Log.e("BADFORMAT", "Data wrongly formatted!");
						if (operation.getFailureListener() != null) {
							operation.getFailureListener().signal(reference);
						}
					}
				});
				return true;
			} catch(Exception e) {
				doNdefClosedSanityCheck();
				Log.d("DEBUG", Log.getStackTraceString(e));
				return false;
			}
		}
		
		public void read(TagReadListener listener) {
			operationQueue_.offer(new TagReadOperation(listener, null, maxTimeout_));
			processOperations();
		}
		
		public void read(TagReadListener successListener, TagReadFailedListener failedListener, long timeout) {
			operationQueue_.offer(new TagReadOperation(successListener, failedListener, timeout));
			processOperations();
		}
		
		public void write(Object toWrite) {
			if (!checkIfWriteOperationNecessary(toWrite)) {
				return;
			}
			operationQueue_.offer(new TagWriteOperation(null, null, maxTimeout_, toWrite));
			processOperations();
		}
		
		public void write(Object toWrite, TagWrittenListener listener) {
			if (!checkIfWriteOperationNecessary(toWrite)) {
				return;
			}
			operationQueue_.offer(new TagWriteOperation(listener, null, maxTimeout_, toWrite));
			processOperations();
		}
		
		public void write(Object toWrite, TagWrittenListener successListener, TagWriteFailedListener failedListener, long timeout) {
			if (!checkIfWriteOperationNecessary(toWrite)) {
				return;
			}
			operationQueue_.offer(new TagWriteOperation(successListener, failedListener, timeout, toWrite));
			processOperations();
		}
		
		public void setReadConverter(NdefMessageToObjectConverter readConverter) {
			ndefMessageToObjectConverter_ = readConverter;
		}
		
		public void setWriteConverter(ObjectToNdefMessageConverter writeConverter) {
			objectToNdefMessageConverter_ = writeConverter;
		}
		
		public void setTag(Tag tag) throws DifferentTagIdException {
			if (tag_.getId() != tag.getId()) {
				throw new DifferentTagIdException();
			}
			tag_ = tag;
		}
		
		private boolean checkIfWriteOperationNecessary(Object toWrite) {
			if (previousCachedDataSerialized_ == null) {
				return true;
			}
			return !(previousCachedDataSerialized_.equals(GSON.toJson(toWrite)));
		}
	}
}
