package edu.vub.at.nfcpoker.ui;


import edu.vub.at.commlib.CommLib;
import edu.vub.at.nfcpoker.Constants;
import edu.vub.at.nfcpoker.R;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QRJoinerActivity extends Activity {
	
	IntentFilter intentFilter;

	protected boolean currentlyJoining = false;
	
	protected static String[] status_strings = {
		"Disabling wireless...",
		"Wireless disabled",
		"Enabling wireless...",
		"Wireless enabled",
		"Wireless state unknown"
	};
	
	private OnClickListener joinerButtonOCL = new OnClickListener() {
		@Override
		public void onClick(View v) {
			currentlyJoining = true;
			Button connectButton = (Button) findViewById(R.id.connect_btn);
			connectButton.setEnabled(false);
			registerReceiver(wifiChangeReceiver, intentFilter);
		}
	};
	
	private BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String newStatus = null;
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int new_wifi_status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				newStatus = status_strings[new_wifi_status];
				String new_network_name = "(Not connected)";

				WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
				
				if (new_wifi_status == WifiManager.WIFI_STATE_DISABLED) {
					publishProgress("Enabling Wireless...");
					wm.setWifiEnabled(true);
				}
				if (new_wifi_status == WifiManager.WIFI_STATE_ENABLED) {
					WifiInfo wi = wm.getConnectionInfo();
					String ssid = wi.getSSID();
					if (ssid != null)
						new_network_name = ssid;
					
					if (currentlyJoining && ssid.equals(wifi_name)) {
						publishProgress("Starting game!");
						startClientActivity();
					} else if (currentlyJoining) {
						publishProgress("Connecting to network...");
						if (wifi_isWD) {
							WifiConfiguration config = new WifiConfiguration();
							config.SSID = '"' + wifi_name + '"';
							config.preSharedKey = '"' + wifi_pass + '"';
							config.hiddenSSID = true;
							config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
							config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
							config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
							config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
							config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
							config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
							
							int id = wm.addNetwork(config);
							Log.d("QRJoiner", "Connecting to network " + wifi_name + ": " + id);
							boolean ret = wm.enableNetwork(id, true);
							Log.d("QRJoiner", "enableNetwork returned " + ret);
						} else {
							boolean joined = false;
							for (WifiConfiguration config : wm.getConfiguredNetworks()) {
								if (config.SSID.equals(wifi_name)) {
									Log.d("QRJoiner", "Found preconfigured network " + wifi_name);
									wm.enableNetwork(config.networkId, true);
									joined = true;
									break;
								}
							}
							if (!joined) {
								Log.d("QRJoiner", "Asking user to connect manually");
								new AlertDialog.Builder(QRJoinerActivity.this)
								    .setMessage("Please connect to the network '" + wifi_name + "' manually and scan the barcode again.")
								    .setCancelable(false)
								    .show();
							}
						}
					}
				}
				TextView network_name = (TextView) findViewById(R.id.network_name);
				network_name.setText(new_network_name);
			}
			if (newStatus != null) {
				TextView status = (TextView) findViewById(R.id.wifi_status);
				status.setText(newStatus);
			}
		}
	};

	protected String wifi_name;
	protected String wifi_pass;
	protected boolean wifi_isWD;
	protected String wifi_server;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrjoiner);
        
        Uri launcher = getIntent().getData();
        wifi_name = launcher.getQueryParameter(Constants.INTENT_WIFI_NAME);
        wifi_pass = launcher.getQueryParameter(Constants.INTENT_WIFI_PASSWORD);
        wifi_isWD = launcher.getQueryParameter(Constants.INTENT_WIFI_IS_DIRECT).equals("true");
        wifi_server = launcher.getQueryParameter(Constants.INTENT_SERVER_IP);
        String join_text = getResources().getString(R.string.qr_code_join_confirmation, wifi_name);
        TextView join_confirmation = (TextView) findViewById(R.id.qr_code_join_text);
        join_confirmation.setText(join_text);
        
        intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		
		Button joinerButton = (Button) findViewById(R.id.connect_btn);
		joinerButton.setOnClickListener(joinerButtonOCL);
    }
    
    

    protected void publishProgress(String string) {
		// TODO Auto-generated method stub
		
	}



	protected void startClientActivity() {
		Intent i = new Intent(this, ClientActivity.class);
		i.putExtra("ip", wifi_server);
		i.putExtra("port", CommLib.SERVER_PORT);
		i.putExtra("isDedicated", false); // todo
		startActivity(i);
		finish();
	}



	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(wifiChangeReceiver );
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(wifiChangeReceiver, intentFilter);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_qrjoiner, menu);
        return true;
    }
	
	public String currentWifiNetwork() {
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		String ssid = wi.getSSID();
		return ssid;
	}
}
