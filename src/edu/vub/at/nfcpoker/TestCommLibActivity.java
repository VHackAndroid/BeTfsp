package edu.vub.at.nfcpoker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class TestCommLibActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_comm_lib);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_test_comm_lib, menu);
        return true;
    }
}
