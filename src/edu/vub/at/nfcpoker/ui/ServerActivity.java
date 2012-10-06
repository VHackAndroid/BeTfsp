package edu.vub.at.nfcpoker.ui;

import edu.vub.at.nfcpoker.ConcretePokerServer;
import edu.vub.at.nfcpoker.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ServerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ConcretePokerServer cps = new ConcretePokerServer(this);
        cps.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server, menu);
        return true;
    }
}
