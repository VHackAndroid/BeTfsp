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
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class Splash extends Activity {

	// TO BE CONFIGURED (TODO)
	public static boolean IS_SERVER = true;

	// Connectivity state
	public static String UUID;
	public static String NETWORK_GROUP;
	
    @Override
    @SuppressWarnings("unused")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Settings
        UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        NETWORK_GROUP = "TODO-FROM-NFC";
        
    }

}
