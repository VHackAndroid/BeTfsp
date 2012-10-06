package edu.vub.at.nfcpoker.ui;

import mobisocial.nfc.Nfc;
import mobisocial.nfc.addon.BluetoothConnector;
import edu.vub.at.nfcpoker.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

public class ClientActivity extends Activity {

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
        setContentView(R.layout.client);
        
        // Settings
        GAME_STATE = GameState.INIT;
        
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
    			Log.d("SENSOR", "" + currentReading);
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
    	            	Toast.makeText(ClientActivity.this, "This is Toast!!!", Toast.LENGTH_SHORT).show();
    	                
    	            }
    	        });
    		}
    	}
    };
}
