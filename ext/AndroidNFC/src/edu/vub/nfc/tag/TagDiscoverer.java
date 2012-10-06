package edu.vub.nfc.tag;

import java.nio.charset.Charset;

import edu.vub.nfc.NFCActivity;

// TagDiscoverer that allows to filter and react on tags detected by an NFCActivity.
public class TagDiscoverer {
	
	private NFCActivity nfcActivity_;
	private byte[] tagType_;
	private NdefMessageToObjectConverter readConverter_;
	private ObjectToNdefMessageConverter writeConverter_;
	
	// Constructor takes an NFCActivity, tagtype and read/write converters.
	// Converters are used to set the converters of all generated TagReferences by this TagDiscoverer.
	public TagDiscoverer(NFCActivity activity, byte[] tagType, NdefMessageToObjectConverter readConverter, ObjectToNdefMessageConverter writeConverter) {
		tagType_ = tagType;
		nfcActivity_ = activity;
		activity.addTagDiscoverer(this);
		readConverter_ = readConverter;
		writeConverter_ = writeConverter;
		activity.addNdefDataTypeToFilterOn(new String(tagType, Charset.forName("UTF-8")));
	}
	
	public NdefMessageToObjectConverter getReadConverter() {
		return readConverter_;
	}
	
	public ObjectToNdefMessageConverter getWriteConverter() {
		return writeConverter_;
	}
	
	public void setTagType(byte[] tagType) {
		tagType_ = tagType;
	}
	
	public byte[] getTagType() {
		return tagType_;
	}
	
	// Cancel discoverer
	public void cancel() {
		nfcActivity_.removeTagDiscoverer(this);
	}
	
	public void checkConditionSetConvertersAndNotify(TagReference tagReference) {
		tagReference.setReadConverter(readConverter_);
		tagReference.setWriteConverter(writeConverter_);
		if (checkCondition(tagReference)) {
			if (tagReference.isNewTag()) {
				onTagDetected(tagReference);
			} else {
				onTagRedetected(tagReference);
			}
		}
	}
	
	// Optionally override to check condition on TagReference
	public boolean checkCondition(TagReference reference) {
		return true;
	}
	
	// Override to react on tag detection.
	public void onTagDetected(TagReference tagReference) { };
	
	// Override to react on tag redetection
	public void onTagRedetected(TagReference tagReference) { };
}
