package edu.vub.at.nfcpoker.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import edu.vub.at.nfcpoker.ui.DiscoveryAsyncTask.DiscoveryCompletionListener;

public class WifiDirectManager extends BroadcastReceiver implements GroupInfoListener, PeerListListener {

	private static WifiDirectManager instance;
	private boolean isRunning;
	private WifiP2pManager manager;
	private Channel channel;
	private Activity act;
	protected DiscoveryCompletionListener dcl;
	private WDMType type;
	private IntentFilter mWifiDirectIntentFilter;
	public WifiP2pGroup mCurrentGroup;
	private boolean alreadyConnecting = false;
	
	enum WDMType { CLIENT, SERVER };

	private WifiDirectManager(Activity act, Looper l, WDMType type) {
		this.isRunning = true;
		this.act = act;
		this.type = type;
		this.manager = (WifiP2pManager) act.getSystemService(Activity.WIFI_P2P_SERVICE);
		if (manager == null) {
			this.isRunning = false;
		} else {
			this.channel = manager.initialize(act, l, null);
			mWifiDirectIntentFilter = new IntentFilter();
		    mWifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		    mWifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		    mWifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		    mWifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		}
	}

	public static WifiDirectManager create(Activity act, Looper mainLooper, boolean isServer) {
		if (instance != null) {
			instance.stop();
			instance = null;
		}
		
		instance = new WifiDirectManager(act, mainLooper, (isServer ? WDMType.SERVER : WDMType.CLIENT));
		return instance;
	}

	private void stop() {
		isRunning = false;
		unregisterReceiver();
	}

	@Override
	public void onGroupInfoAvailable(WifiP2pGroup group) {
		if (group == null)
			return;
		
		Log.d("Wifi-Direct", "Got group info!" + group);
		if (isRunning) {
			WifiP2pDevice device = group.getOwner();
			final String groupName = group.getNetworkName();
			final String password = group.getPassphrase();
			mCurrentGroup = group;
			//TODO: advertise password for legacy clients.
			Log.d("Wifi-Direct", "Created group " + groupName + " with password '" + password + "'");
			act.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(act, "Created group " + groupName + " with password '" + password + "'", Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	public void discover(DiscoveryCompletionListener dcl) {
		Log.d("Wifi-Direct", "Starting discovery...");
		this.dcl = dcl;
	}

	public void createGroup(final Runnable r) {
		Log.d("Wifi-Direct", "Creating group...");
		manager.createGroup(this.channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				Log.d("Wifi-Direct", "Group created!");
				manager.requestGroupInfo(channel, WifiDirectManager.this);
				r.run();
			}
			
			@Override
			public void onFailure(int reason) {
				Log.d("Wifi-Direct", "Group creation failed:" + reason);
			}
		});
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		final int numDevices = peers.getDeviceList().size();
		Log.d("Wifi-Direct", "Got peer list: " + numDevices);
		if (numDevices != 1 || alreadyConnecting)
			return;					
		
		alreadyConnecting  = true;
		WifiP2pDevice first = peers.getDeviceList().iterator().next();
		Log.d("Wifi-Direct", "Single device found: " + first);
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = first.deviceAddress;
		manager.connect(channel, config, new ActionListener() {
			
			@Override
			public void onSuccess() {
				Log.d("Wifi-Direct", "Connected to device!");
				act.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						stop();
						new DiscoveryAsyncTask(act, dcl).execute();
					}
				});
			}
			
			@Override
			public void onFailure(int reason) {
				Log.d("Wifi-Direct", "Connection to device failed :'(");
			}
		});
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (type != WDMType.CLIENT || !isRunning)
			return;
		String action = intent.getAction();
		Log.d("Wifi-Direct", "Receved intent: " + action);
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        	manager.requestPeers(this.channel, this);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
	}
	
	public void registerReceiver() {
		act.registerReceiver(this, mWifiDirectIntentFilter);
		manager.discoverPeers(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				Log.d("Wifi-Direct", "Started peer discovery...");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.d("Wifi-Direct", "Peer discovery failed: " + reason);
			}
		});
	}
	
	public void unregisterReceiver() {
		act.unregisterReceiver(this);
	}

}
