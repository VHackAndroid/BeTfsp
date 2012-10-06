package edu.vub.at.nfcpoker;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import edu.vub.at.commlib.CommLibConnection;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class TestCommLibActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_comm_lib);
        Log.d("TCLA", "Activity started");
        
        String ip = getIntent().getStringExtra("ip");
        int port = getIntent().getIntExtra("port", 54555);
        
        
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_test_comm_lib, menu);
        return true;
    }
}
