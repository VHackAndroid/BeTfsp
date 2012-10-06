package edu.vub.nfc;

import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.vub.AndroidNFC.R;
import edu.vub.nfc.beam.Beamer;
import edu.vub.nfc.beam.listener.BeamFailedListener;
import edu.vub.nfc.beam.listener.BeamReceivedListener;
import edu.vub.nfc.beam.listener.BeamSuccessListener;
import edu.vub.nfc.tag.NdefMessageToObjectConverter;
import edu.vub.nfc.tag.ObjectToNdefMessageConverter;
import edu.vub.nfc.tag.TagDiscoverer;
import edu.vub.nfc.tag.TagReference;
import edu.vub.nfc.tag.listener.TagReadFailedListener;
import edu.vub.nfc.tag.listener.TagReadListener;
import edu.vub.nfc.tag.listener.TagWriteFailedListener;
import edu.vub.nfc.tag.listener.TagWrittenListener;


public class AndroidNFCActivity extends NFCActivity {
	
	public static final byte[] THING_TYPE = "application/thing".getBytes(Charset.forName("UTF-8"));
	private TagReference tagReference_ = null;
	private long readTimeOut_ = 5000;
	private long writeTimeOut_ = 5000;
	
	private Beamer beamer_ = new Beamer(this, new StringToNdefMessageConverter());
	
	private OnClickListener saveButtonListener = new OnClickListener() {
	    public void onClick(View button) {
	    	if (tagReference_ == null) {
	    		handleTagWriteFailed();
	    		return;
	    	}
	    	EditText newContentsEditText = (EditText) findViewById(R.id.newContents);
	    	String toWrite = newContentsEditText.getText().toString();
	    	tagReference_.write(
	    			toWrite, 
	    			new TagWrittenListener() {
						@Override
						public void signal(TagReference tagReference) {
							handleTagRead(tagReference);
						}
					}, 
	    			new TagWriteFailedListener() {
						@Override
						public void signal(TagReference tagReference) {
							handleTagWriteFailed();
						}
					}, 
	    			writeTimeOut_);
	    }
	};
	
	private OnClickListener beamButtonListener = new OnClickListener() {
	    public void onClick(View button) {
	    	EditText newContentsEditText = (EditText) findViewById(R.id.newContents);
	    	String toBeam = newContentsEditText.getText().toString();
	    	// Beaming is undirected.
	    	beamer_.beam(
	    			toBeam,
	    			new BeamSuccessListener() {
						@Override
						public void signal() {
							handleBeamSucceeded();
						}
					},
					new BeamFailedListener() {
						@Override
						public void signal() {
							handleBeamFailed();
						}
					},
					writeTimeOut_);
	    }
	};
	
	private class NdefMessageToStringConverter implements NdefMessageToObjectConverter {
		@Override
		public Object convert(NdefMessage ndefMessage) {
			return new String((ndefMessage.getRecords()[0]).getPayload());
		}
	};
	
	private class StringToNdefMessageConverter implements ObjectToNdefMessageConverter {
		@Override
		public NdefMessage convert(Object o) {
			String toConvert;
			if (o == null) {
				toConvert = "";
			} else {
				toConvert = (String)o;
			}
			byte[] dataBytes = toConvert.getBytes(Charset.forName("UTF-8"));
		    byte[] id = new byte[0]; //We don’t use the id field
		    NdefRecord r = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, THING_TYPE, id, dataBytes);
		    return new NdefMessage(new NdefRecord[]{ r });
		}
	};
	
	
	private void handleTagRead(TagReference tagReference) {
		EditText oldContentsEditText = (EditText) findViewById(R.id.oldContents);
		oldContentsEditText.setText((String)tagReference.getCachedData());
	}
	
	private void handleTagReadFailed() {
		EditText oldContentsEditText = (EditText) findViewById(R.id.oldContents);
		oldContentsEditText.setText("Failed to read tag, try again.");
	}
	
	private void handleTagWriteFailed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Writing to tag failed, try again.")
		       .setCancelable(false)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		builder.create().show();
	}
	
	private void handleBeamFailed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Beaming failed, try again.")
		       .setCancelable(false)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		builder.create().show();
	}
	
	private void handleBeamSucceeded() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Beaming succeeded!")
		       .setCancelable(false)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		builder.create().show();
	}
	
	private void readTagAndUpdateUI(TagReference tagReference) {
		tagReference_ = tagReference;
		tagReference.read(
	       		new TagReadListener() {
					@Override
					public void signal(TagReference tagReference) {
						handleTagRead(tagReference);
					}
				}, 
	       		new TagReadFailedListener() {
					@Override
					public void signal(TagReference tagReference) {
						handleTagReadFailed();
					}
				}, 
	       		readTimeOut_);
	}
	
	public class MyTagDiscoverer extends TagDiscoverer {
		
		public MyTagDiscoverer(NFCActivity activity, byte[] tagType, NdefMessageToObjectConverter readConverter, ObjectToNdefMessageConverter writeConverter) {
			super(activity, tagType, readConverter, writeConverter);
		}
		
		@Override
		public void onTagDetected(TagReference tagReference) {
			readTagAndUpdateUI(tagReference);
		}
		
		@Override
		public void onTagRedetected(TagReference tagReference) {
			readTagAndUpdateUI(tagReference);
		}
	}
	
	private class MyBeamListener extends BeamReceivedListener {
		
		public MyBeamListener(NFCActivity activity, NdefMessageToObjectConverter converter) {
			super(activity, converter);
		}
		
		@Override
		public void onBeamReceived(Object o) {
			EditText oldContentsEditText = (EditText) findViewById(R.id.oldContents);
			oldContentsEditText.setText((String) o);
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Important: use correct constructor.
        super.onCreate(savedInstanceState, new StringToNdefMessageConverter());
        setContentView(R.layout.main);
        
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);
        Button beamButton = (Button) findViewById(R.id.beamButton);
        beamButton.setOnClickListener(beamButtonListener);
        
        new MyTagDiscoverer(this, THING_TYPE, new NdefMessageToStringConverter(), new StringToNdefMessageConverter());
        new MyBeamListener(this, new NdefMessageToStringConverter());
    }
}