package edu.vub.at.nfcpoker.ui;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.R.layout;
import edu.vub.at.nfcpoker.R.menu;

import nfc.pairing.AsciiNdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.Menu;
import android.widget.ImageView;

public class Splash extends Activity {

	// TO BE CONFIGURED (TODO)
	public static boolean IS_SERVER = true;

	// Connectivity state
	public static String UUID;
	public static String NETWORK_GROUP;
	
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
        
        // UI
        /*final Intent intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED);
        intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, AsciiNdefMessage.CreateNdefMessage(UUID));
        startActivity(intent);*/
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash, menu);
        return true;
    }
}
