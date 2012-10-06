package edu.vub.at.nfcpoker.ui;

import edu.vub.at.nfcpoker.Card;
import edu.vub.at.nfcpoker.ConcretePokerServer;
import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.ui.tools.PageProvider;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    
    int nextToReveal = 0;
    
	public void revealCards(final Card[] cards) {
		runOnUiThread(new Runnable() {
			public void run() {
				for (Card c : cards) {
					Log.d("PokerServer", "Revealing card " + c);
					LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
					ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
					ib.setImageResource(cardToResourceID(c));
				}
			}

			public int cardToResourceID(Card c) {
				return getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + c.toString(), null, null);
			}
		});
	}
	

	public void resetCards() {
		Log.d("PokerServer", "Hiding cards again");
		nextToReveal = 0;
		runOnUiThread(new Runnable() {
			public void run() {
				LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
				for (int i = 0; i < 5; i++) {
					ImageButton ib = (ImageButton) ll.getChildAt(i);
					ib.setImageResource(R.drawable.backside);
				}
			}
		});
	}

	public void showStatechange(final GameState newState) {
		runOnUiThread(new Runnable() {
			public void run() {
				TextView phase = (TextView)findViewById(R.id.current_phase);
				phase.setText(newState.toString());
			}
		});
	};
}
