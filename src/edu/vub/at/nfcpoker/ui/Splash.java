package edu.vub.at.nfcpoker.ui;

import mobisocial.nfc.Nfc;
import mobisocial.nfc.addon.BluetoothConnector;
import mobisocial.nfc.addon.BluetoothConnector.OnConnectedListener;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.R.layout;
import edu.vub.at.nfcpoker.R.menu;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class Splash extends Activity {

	// TO BE CONFIGURED (TODO)
	public static boolean IS_SERVER = true;

	// Connectivity state
	public static String UUID;
	public static String NETWORK_GROUP;
    private Nfc mNfc;
    private Long mLastPausedMillis = 0L;
    
    // Interactivity
    private int incognitoSensors;
	
	// Game state
	public static GameState GAME_STATE;
	
	// Enums
	public enum GameState {
	    INIT, NFCPAIRING, FLOP, TURN, RIVER
	}
	
    @Override
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Settings
        UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        NETWORK_GROUP = "TODO-FROM-NFC";
        GAME_STATE = GameState.INIT;
        
        // Connectivity
        mNfc = new Nfc(this);
        // If this activity was launched from an NFC interaction, start the
        // Bluetooth connection process.
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            BluetoothConnector.join(mNfc, mBluetoothConnected, getNdefMessages(getIntent())[0]);
        } else {
            // If both phones are running this activity, or to allow remote
            // device to join from home screen.
            BluetoothConnector.prepare(mNfc, mBluetoothConnected, getAppReference());
        }
        
        
        // Interactivity
        incognitoSensors = 0;
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
	        sensorManager.registerListener(incognitoSensorEventListener, 
	        		lightSensor, 
	        		SensorManager.SENSOR_DELAY_NORMAL);
	        incognitoSensors++;
        }
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
	        sensorManager.registerListener(incognitoSensorEventListener, 
	        		proximitySensor, 
	        		SensorManager.SENSOR_DELAY_NORMAL);
	        incognitoSensors++;
        }
        
        // UI
        /*final Intent intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED);
        intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, AsciiNdefMessage.CreateNdefMessage(UUID));
        startActivity(intent);*/
        setContentView(R.layout.client);
        
    }
    
    SensorEventListener incognitoSensorEventListener = new SensorEventListener() {
    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		
    	}

    	@Override
    	public void onSensorChanged(SensorEvent event) {
    		int incognito = 0;
    		if (event.sensor.getType()==Sensor.TYPE_LIGHT){
    			float currentReading = event.values[0];
    			if (currentReading < event.sensor.getMaximumRange() / 10) {
    				incognito++;
    			}
    		}
    		if (event.sensor.getType()==Sensor.TYPE_PROXIMITY){
    			float currentReading = event.values[0];
    			if (currentReading < event.sensor.getMaximumRange() / 10) {
    				incognito++;
    			}
    		}
    		if (incognito >= incognitoSensors) {
    			runOnUiThread(new Runnable() {

    	            @Override
    	            public void run() {
    	                // Toast.makeText(YourActivityName.this, "This is Toast!!!", Toast.LENGTH_SHORT).show();
    	                
    	            }
    	        });
    		}
    	}
    };
    
    
    
    protected void onResume() {
        super.onResume();
        mNfc.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        mLastPausedMillis = System.currentTimeMillis();
        mNfc.onPause(this);
    }

    protected void onNewIntent(Intent intent) {
        // Check for "warm boot" if the activity uses singleInstance launch mode:
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Long ms = System.currentTimeMillis() - mLastPausedMillis;
            if (ms > 150) {
                BluetoothConnector.join(mNfc, mBluetoothConnected, getNdefMessages(intent)[0]);
                return;
            }
        }
        if (mNfc.onNewIntent(this, intent)) {
            return;
        }
    }

    public NdefRecord[] getAppReference() {
        byte[] urlBytes = "http://example.com/funapp".getBytes();
        NdefRecord ref = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI, new byte[]{}, urlBytes);
        return new NdefRecord[] { ref };
    }

    OnConnectedListener mBluetoothConnected = new OnConnectedListener() {
        public void onConnectionEstablished(BluetoothSocket socket, boolean isServer) {
            toast("connected! server: " + isServer);
            // TODO DRIES
        }

		public void beforeConnect(boolean isServer) {
			
		}
    };

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Splash.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        if (!intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
            return null;
        }
        Parcelable[] msgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] ndef = new NdefMessage[msgs.length];
        for (int i = 0; i < msgs.length; i++) {
            ndef[i] = (NdefMessage) msgs[i];
        }
        return ndef;
    }
}
