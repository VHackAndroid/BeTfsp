package edu.vub.at.nfcpoker;

import java.io.IOException;

import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.commlib.DiscoveryListener;
import edu.vub.at.nfcpoker.comm.PokerServer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
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
				Intent i = new Intent(Splash.this, TestCommLibActivity.class);
				i.putExtra("ip", result.getAddress());
				i.putExtra("port", Integer.parseInt(result.getPort()));
				startActivity(i);
			} else {
				Toast.makeText(Splash.this, "Could not discover hosts", Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Button discover = (Button) findViewById(R.id.discover);
        discover.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new DiscoveryAsyncTask().execute();
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash, menu);
        return true;
    }
}
