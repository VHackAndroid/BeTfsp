package edu.vub.at.nfcpoker.ui;

import java.io.IOException;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.comm.PokerServer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Splash extends Activity {

    public class DiscoveryAsyncTask extends AsyncTask<Void, Void, CommLibConnectionInfo> {

		@Override
		protected CommLibConnectionInfo doInBackground(Void... arg0) {
			WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
			MulticastLock ml = wm.createMulticastLock("edu.vub.at.nfcpoker");
			ml.acquire();
			try {
				return CommLib.discover(PokerServer.class);
			} catch (IOException e) {
				Log.d("Discovery", "Could not start discovery", e);
				return null;
			} finally {
				ml.release();
			}
		}

		@Override
		protected void onPostExecute(CommLibConnectionInfo result) {
			super.onPostExecute(result);
			if (result != null) {
				Intent i = new Intent(Splash.this, ClientActivity.class);
				i.putExtra("ip", result.getAddress());
				i.putExtra("port", Integer.parseInt(result.getPort()));
				startActivity(i);
			} else {
				Toast.makeText(Splash.this, "Could not discover hosts", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// TO BE CONFIGURED (TODO)
	public static boolean IS_SERVER = true;
	public static boolean IS_LODE = false;

	// Connectivity state
	public static String UUID;
	public static String NETWORK_GROUP;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Settings
        UUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        NETWORK_GROUP = "TODO-FROM-NFC";
        
        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new DiscoveryAsyncTask().execute();
			}
		});

        if (IS_LODE) {
        	Intent intent = new Intent(this, ClientActivity.class);
        	startActivity(intent);
        } else {
        	Button server = (Button) findViewById(R.id.server);
        	if (server != null)
        		server.setOnClickListener(new OnClickListener() {

        			@Override
        			public void onClick(View v) {
        				startServer();
        			}
        		});
        }
	}

    protected void startServer() {
		Intent i = new Intent(this, ServerActivity.class);
		startActivity(i);
		finish();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash, menu);
        return true;
    }
}
