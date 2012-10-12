package edu.vub.at.nfcpoker.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import edu.vub.at.commlib.CommLib;
import edu.vub.at.commlib.CommLibConnectionInfo;
import edu.vub.at.nfcpoker.ConcretePokerServer;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.comm.PokerServer;
import edu.vub.at.nfcpoker.settings.Settings;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.vub.at.nfcpoker.TableThing;
import edu.vub.nfc.thing.EmptyRecord;
import edu.vub.nfc.thing.Thing;
import edu.vub.nfc.thing.ThingActivity;
import edu.vub.nfc.thing.listener.ThingSavedListener;

public class Splash extends ThingActivity<TableThing> {

	public class DiscoveryAsyncTask extends AsyncTask<Void, Void, CommLibConnectionInfo> {
		
		@Override
		protected CommLibConnectionInfo doInBackground(Void... arg0) {
			WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
			MulticastLock ml = wm.createMulticastLock("edu.vub.at.nfcpoker");
			ml.acquire();
			while (true) {
				try {
					Log.d("Discovery", "Discovering... (" + broadcastAddress + ")");
					CommLibConnectionInfo c = CommLib.discover(PokerServer.class, broadcastAddress);
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
				if (client_startClientServerTimer != null) {
					client_startClientServerTimer.cancel();
					client_startClientServerTimer = null;
					if (client_startClientServerAsk != null) {
						client_startClientServerAsk.dismiss();
						client_startClientServerAsk = null;
					}
				}
				Intent i = new Intent(Splash.this, ClientActivity.class);
				i.putExtra("ip", result.getAddress());
				i.putExtra("port", Integer.parseInt(result.getPort()));
				i.putExtra("isDedicated", result.isDedicated());
				startActivity(i);
			} else {
				Toast.makeText(Splash.this, "Could not discover hosts", Toast.LENGTH_SHORT).show();
			}
		}
	}


	private static final boolean LODE = false;

	// Shared globals
	public static final String WEPOKER_WEBSITE = "http://wepoker.info";

	// Connectivity state
	public static volatile String ipAddress;
	public static volatile String broadcastAddress;
	private BroadcastReceiver wifiWatcher;
	
	// Discovery
	private volatile DiscoveryAsyncTask discoveryTask;
	private volatile Timer client_startClientServerTimer;
	private volatile Dialog client_startClientServerAsk;
	
	// UI
	public static Handler messageHandler;
	private static boolean isTablet;
	private int startClientServerTimerTimeout = 10000;
	private int startClientServerTimerTimeout2 = 30000;

	// NFC
	private Object lastScannedTag_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		// Settings
		Settings.loadSettings(this);

//		Button beamButton = (Button) findViewById(R.id.beamInviteButton);
//		beamButton.setEnabled(false);
//		beamButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				((TableThing) lastScannedTag_).broadcast();
//			}
//		});
		
		View tablet_layout = findViewById(R.id.tablet_layout);
		if (tablet_layout != null) isTablet = true;
		
		Button server = (Button) findViewById(R.id.server);
		if (server != null)
			server.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startServer();
				}
			});

		// NFC
		Button nfc = (Button) findViewById(R.id.nfc);
		if (nfc != null) {
			final Dialog nfc_dialog = createNFCDialog();
			nfc.setEnabled(false);
			nfc.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					nfc_dialog.show();
				}
			});
		}

		// UI
		messageHandler = new IncomingHandler(this);
		
		
		if (LODE) {
			Intent i = new Intent(this, ClientActivity.class);
			i.putExtra("isDedicated", false);
			startActivity(i);
			return;
		}
		
		registerWifiWatcher();
		updateIpAddress(this);

		if (!isTablet) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			discoveryTask = new DiscoveryAsyncTask();
			discoveryTask.execute();
			// If there is no server responding after 10 seconds, ask the user to start one without a dedicated table
			scheduleAskStartClientServer(startClientServerTimerTimeout);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			final Button disc = (Button) findViewById(R.id.discover_button);
			disc.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					new DiscoveryAsyncTask().execute();
					disc.setEnabled(false);
				}
			});
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerWifiWatcher();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(wifiWatcher); wifiWatcher = null;
		// TODO pause discovery 
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Settings.saveSettings(this);
	}
	
	// Connectivity
	private class ConnectionChangeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent ) {
			Log.d("wePoker", "Wifi Broadcast Reciever");
			updateIpAddress(context);
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
	
	private void updateIpAddress(Context ctx) {
		ipAddress = CommLib.getIpAddress(this);
		broadcastAddress = CommLib.getBroadcastAddress(this);
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
	    
	
	// Discovery
	private void scheduleAskStartClientServer(int timeout) {
		client_startClientServerTimer = new Timer();
		client_startClientServerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						askStartClientServer();
					}
				});
			}
		}, timeout);
	}
	
	private void askStartClientServer() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					if (client_startClientServerTimer != null) {
						client_startClientServerTimer.cancel();
						client_startClientServerTimer = null;
						new Thread() {
							@Override
							public void run() {
						    	ConcretePokerServer cps = new ConcretePokerServer(
						    			new DummServerView(), false);
						    	cps.start();
							}
						}.start();
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					scheduleAskStartClientServer(startClientServerTimerTimeout2);
					break;
				}
				client_startClientServerAsk = null;
			}
		};
		
		DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				scheduleAskStartClientServer(startClientServerTimerTimeout2);
				client_startClientServerAsk = null;
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		client_startClientServerAsk =
				builder.setMessage("No Ambient-Poker game discovered.\nDo you wish to start one?")
				.setOnCancelListener(onCancelListener)
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).create();
		client_startClientServerAsk.show();
	}

	// NFC
	private Dialog createNFCDialog() {
		final Splash theActivity = this;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.dialog_signin, null));
		final AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Write", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d, int id) {
				try {
					String iptext = ((EditText) dialog.findViewById(R.id.ip)).getText().toString();
					Integer port = Integer.parseInt(((EditText) dialog.findViewById(R.id.port)).getText().toString());
					TableThing tableThing = new TableThing(theActivity, iptext, port.intValue());
					if (lastScannedTag_ == null) {
						Toast.makeText(theActivity, "Scan a tag first", Toast.LENGTH_SHORT).show();
					}
					if (lastScannedTag_ instanceof EmptyRecord) {
						((EmptyRecord) lastScannedTag_).initialize(tableThing, new ThingSavedListener<TableThing>() {
							@Override
							public void signal(TableThing savedTableThing) {
								lastScannedTag_ = savedTableThing;
								Toast.makeText(theActivity, "NFC tag written successfully", Toast.LENGTH_SHORT).show();
							}
						});
					} else {
						TableThing scannedTableThing = (TableThing) lastScannedTag_;
						scannedTableThing.ip_ = iptext;
						scannedTableThing.port_ = port;
						scannedTableThing.saveAsync(new ThingSavedListener<TableThing>() {
							@Override
							public void signal(TableThing savedTableThing) {
								Toast.makeText(theActivity, "NFC tag written successfully", Toast.LENGTH_SHORT).show();
							}
						});
					}
				} catch (Exception e) {
					Toast.makeText(theActivity, "Failed to write NFC tag, verify IP and port information", Toast.LENGTH_SHORT).show();
					Log.d("NFC-TAG", "Failed to write NFC tag", e);
				}
			}
		});
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int id) {
				dialog.cancel();
			}
		});
		return dialog;
	}

	@Override
	public void whenDiscovered(TableThing tableThing) {
		super.whenDiscovered(tableThing);
		Button nfcButton = (Button) findViewById(R.id.nfc);
		nfcButton.setEnabled(true);
		lastScannedTag_ = tableThing;
		startClientNFC(tableThing);
	}

	@Override
	public void whenDiscovered(EmptyRecord r) {
		super.whenDiscovered(r);
		Button nfcButton = (Button) findViewById(R.id.nfc);
		nfcButton.setEnabled(true);
		lastScannedTag_ = r;
	}

	protected void startServer() {
		if (discoveryTask != null) {
			discoveryTask.cancel(true);
			discoveryTask = null;
		}
		Intent i = new Intent(this, ServerActivity.class);
		startActivity(i);
		finish();
	}
	
	protected void startClientNFC(TableThing tag) {
		if (tag != null) {
			if (discoveryTask != null) {
				discoveryTask.cancel(true);
				discoveryTask = null;
			}
			Intent i = new Intent(this, ClientActivity.class);
			i.putExtra("ip", tag.ip_);
			i.putExtra("port", tag.port_);
			i.putExtra("isDedicated", false);
			startActivity(i);
			finish();
		}
	}

	@Override
	public Class<? extends Thing> getThingType() {
		return TableThing.class;
	}
}

