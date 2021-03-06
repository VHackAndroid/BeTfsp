/*
 * wePoker: Play poker with your friends, wherever you are!
 * Copyright (C) 2012, The AmbientTalk team.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.vub.at.nfcpoker.ui;

import java.lang.ref.WeakReference;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.Constants;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.WePokerPreferencesActivity;
import edu.vub.at.nfcpoker.comm.DiscoveryAsyncTask;
import edu.vub.at.nfcpoker.settings.Settings;

public class Splash extends Activity {

	// Shared globals
	public static final String WEPOKER_WEBSITE = "http://wepoker.info";

	// Connectivity state
	private BroadcastReceiver wifiWatcher;
	
	// Discovery
	private volatile DiscoveryAsyncTask discoveryTask;
	
	// UI
	public static Activity activity;
	public static Handler messageHandler;
	private boolean isTablet = false;
	private boolean isTV = false;
	
	// Debug
	private final boolean debugGUI = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splash);
		activity = this;

		// Settings
		Settings.loadSettings(this);
		
		
		View tablet_layout = findViewById(R.id.tablet_layout);
		if (tablet_layout != null)
			isTablet = true;
		
		if (getPackageManager().hasSystemFeature("com.google.android.tv"))
			isTV = true;
		
		// UI
		messageHandler = new IncomingHandler(this);
		
		final Button joinGame = (Button) findViewById(R.id.JoinGame);
		if (joinGame != null) {
			joinGame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopDiscovery();
					askJoinGame();
				}
			});
		}
		
		final Button createGame = (Button) findViewById(R.id.CreateGame);
		if (createGame != null) {
			createGame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopDiscovery();
					startServer(false);
				}
			});
		}
		
		final Button createAdHocGame = (Button) findViewById(R.id.CreateAdHocGame);
		if (createAdHocGame != null) {
			createAdHocGame.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopDiscovery();
					startServer(true);
				}
			});
		}
		
		// Default connectivity actions
		if (isTV) {
			// TV => Server
			startServer(false);
		} else if (!isTablet) {
			// Phone => Background discovery with pop-up to confirm
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			startDiscovery(activity);
		} else {
			// Tablet => Always offer the choice
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		
		// Debug
		if (debugGUI) {
			Intent i = new Intent(this, ClientActivity.class);
			i.putExtra(Constants.INTENT_IS_SERVER, true);
			i.putExtra(Constants.INTENT_IS_DEDICATED, false);
			startActivity(i);
		}
	}
	
	public boolean isWifiDirectSupported() {
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
	}
	
	public boolean isNFCSupported() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		activity = this;
		registerWifiWatcher();
		startDiscovery(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(wifiWatcher); wifiWatcher = null;
		stopDiscovery();
		activity = null;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Settings.saveSettings(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemSettings:
			Intent i = new Intent(this, WePokerPreferencesActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_splash, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {	
			Intent i = new Intent(this, QRJoinerActivity.class);
			final String scannedUri = scanResult.getContents();
			if (scannedUri == null) return;
			if (!scannedUri.startsWith("http://wepoker.info/play")) {
				Toast.makeText(this, "Please scan a QR code that belongs to a wePoker game", Toast.LENGTH_SHORT).show();
			} else {
				i.setData(Uri.parse(scannedUri));
				startActivity(i);
			}
		}
	}

	// Connectivity
	private class ConnectionChangeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent ) {
			Log.d("wePoker - Splash", "My IP Address changed!");
			if (activity != null) {
				startDiscovery(activity);
			}
		}
	}
	
	private void registerWifiWatcher() {
		if (wifiWatcher != null) return;
		wifiWatcher = new ConnectionChangeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		registerReceiver(wifiWatcher, intentFilter);
	}
	
	// UI
	static class IncomingHandler extends Handler {
		private final WeakReference<Context> mCtx;

		IncomingHandler(Context ctx) {
			mCtx = new WeakReference<Context>(ctx);
		}

		@Override
		public void handleMessage(Message msg) {
			String txt;
			Context ctx = mCtx.get();
			if (ctx != null) {
				switch(msg.what) {
				case UIMessage.MESSAGE_TOAST:
					txt = msg.getData().getString("message");
					if (txt == null) return;
					Toast.makeText(ctx, txt, Toast.LENGTH_SHORT).show();
					break;
				case UIMessage.MESSAGE_DISCOVERY_FAILED:
					// TODO
					break;
				}
			}
		}
	}

	private void askJoinGame() {
		if (activity == null) return;
		final Dialog dialog = new Dialog(activity);
		dialog.setContentView(R.layout.join_game);
		dialog.setTitle("Select connectivity option");
		
		final Button btnJoinQRCode = (Button) dialog.findViewById(R.id.JoinDialogQRCode);
		btnJoinQRCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(Splash.this);
				integrator.initiateScan();
			}
		});

		final Button btnJoinRFID = (Button) dialog.findViewById(R.id.JoinDialogRFID);
		btnJoinRFID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Explain RFID graphically / animation
				dialog.dismiss();
			}
		});

		final Button btnJoinOnline = (Button) dialog.findViewById(R.id.JoinDialogOnline);
		btnJoinOnline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(activity, "Online games are not yet supported", Toast.LENGTH_LONG).show();
				dialog.dismiss();
			}
		});

		final Button btnJoinDiscovery = (Button) dialog.findViewById(R.id.JoinDialogDiscovery);
		btnJoinDiscovery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(activity, DiscoveryActivity.class));
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	protected void startServer(boolean forceWifiDirect) {
		if (activity == null) return;
		stopDiscovery();
		
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		wm.setWifiEnabled(true);
		WifiInfo connInfo = wm.getConnectionInfo();
		boolean enabled = wm.isWifiEnabled();
		boolean connected = connInfo != null && connInfo.getNetworkId() != -1;
		boolean isDedicated = isTV || isTablet;
		boolean preferWifiDirect = Settings.isWifiDirectPreferred();
		boolean currentlyConnected = enabled && connected;
		boolean shouldUseWifiDirect = isWifiDirectSupported() && (forceWifiDirect || preferWifiDirect || !currentlyConnected);
		Intent i;
		if (isDedicated) {
			i = new Intent(this, ServerActivity.class);
		} else {
			i = new Intent(this, ClientActivity.class);
		}
		i.putExtra(Constants.INTENT_WIFI_DIRECT, shouldUseWifiDirect);
		i.putExtra(Constants.INTENT_IS_DEDICATED, isDedicated);
		i.putExtra(Constants.INTENT_IS_SERVER, true);
		if (!isWifiDirectSupported() && !currentlyConnected) {
			Toast.makeText(this, "Wifi disconnected and Wifi-Direct is not supported on this device. Please connect to the nearest Wifi hotspot and try again!", Toast.LENGTH_SHORT).show();
			return;
		}
		startActivity(i);
		finish();
	}
	
	// Discovery
	public void startDiscovery(Activity act) {
		if (discoveryTask != null)
			discoveryTask.cancel(true);

		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo connInfo = wm.getConnectionInfo();
		boolean enabled = wm.isWifiEnabled();
		boolean connected = connInfo != null && connInfo.getNetworkId() != -1;
		if (!enabled || !connected) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideDiscoveryInformation();
				}
			});
			return;
		}

		final DiscoveryAsyncTask.DiscoveryCompletionListener dcl = new DiscoveryAsyncTask.DiscoveryCompletionListener() {
			@Override
			public void onDiscovered(CommLibConnectionInfo result) {
				stopDiscovery();
				int port = CommLib.SERVER_PORT;
				try {
					Integer.parseInt(result.getPort());
				} catch (Exception e) { }
				WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
				String wifiName = CommLib.getWifiGroupName(wm);
				String wifiPass = CommLib.getWifiPassword(wifiName);
				askJoinDiscoveredServer(result.getAddress(), port, result.isDedicated(), false, null, wifiName, wifiPass);
			}
		};
		
		discoveryTask = new DiscoveryAsyncTask(act, dcl);
		discoveryTask.execute();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDiscoveryInformation();
			}
		});
	}
	
	private void showDiscoveryInformation() {
		final ProgressBar pb = (ProgressBar) findViewById(R.id.ConnectionInformationProgressBar);
		if (pb != null) {
			pb.setVisibility(View.VISIBLE);
		}
		final TextView tv = (TextView) findViewById(R.id.ConnectionInformation);
		if (tv != null) {
			tv.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideDiscoveryInformation() {
		final ProgressBar pb = (ProgressBar) findViewById(R.id.ConnectionInformationProgressBar);
		if (pb != null) {
			pb.setVisibility(View.INVISIBLE);
		}
		final TextView tv = (TextView) findViewById(R.id.ConnectionInformation);
		if (tv != null) {
			tv.setVisibility(View.INVISIBLE);
		}
	}
	
	public void stopDiscovery() {
		if (discoveryTask == null) return;
		discoveryTask.cancel(true);
		discoveryTask = null;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideDiscoveryInformation();
			}
		});
	}
	
	private void askJoinDiscoveredServer(
			final String ip, final int port, final boolean isDedicated,
			final boolean isServer, final String broadcast, final String wifiName, final String wifiPassword) {
		if (activity == null) return;
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					ClientActivity.startClient(activity, ip, port, isDedicated, isServer, broadcast, wifiName, wifiPassword);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
				}
			}
		};
		
		DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dg =
				builder.setMessage("wePoker game discovered!\nDo you wish to join it?")
				.setOnCancelListener(onCancelListener)
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).create();
		dg.show();
	}
}

