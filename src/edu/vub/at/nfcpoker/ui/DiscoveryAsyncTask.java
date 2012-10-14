package edu.vub.at.nfcpoker.ui;

import java.io.IOException;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.comm.PokerServer;
import edu.vub.at.nfcpoker.ui.DiscoveryAsyncTask.DiscoveryCompletionListener;

public class DiscoveryAsyncTask extends AsyncTask<Void, Void, CommLibConnectionInfo> {
	public interface DiscoveryCompletionListener {
		void onDiscovered(CommLibConnectionInfo result);
	}

	private final Activity act;
	private DiscoveryAsyncTask.DiscoveryCompletionListener dcl;

	public DiscoveryAsyncTask(Activity act, DiscoveryAsyncTask.DiscoveryCompletionListener dcl) {
		this.act = act;
		this.dcl = dcl;
	}
	
	@Override
	protected CommLibConnectionInfo doInBackground(Void... arg0) {
		WifiManager wm = (WifiManager) this.act.getSystemService(Splash.WIFI_SERVICE);
		MulticastLock ml = wm.createMulticastLock("edu.vub.at.nfcpoker");
		ml.acquire();
		while (true) {
			try {
				CommLibConnectionInfo c = CommLib.discover(PokerServer.class, Splash.broadcastAddress);
				ml.release();
				return c;
			} catch (IOException e) {
				Log.d("Discovery", "Could not start discovery", e);
			}
			try { Thread.sleep(2000);
			} catch (InterruptedException e1) { }
		}
	}

	@Override
	protected void onPostExecute(CommLibConnectionInfo result) {
		super.onPostExecute(result);
		if (result != null) {
			dcl.onDiscovered(result);
		} else {
			Toast.makeText(this.act, "Could not discover hosts", Toast.LENGTH_SHORT).show();
		}
	}
}