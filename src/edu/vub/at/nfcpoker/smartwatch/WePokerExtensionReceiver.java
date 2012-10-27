package edu.vub.at.nfcpoker.smartwatch;

import com.esotericsoftware.minlog.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The extension receiver receives the extension intents and starts the
 * extension service when it arrives.
 */
public class WePokerExtensionReceiver extends BroadcastReceiver {

    public WePokerExtensionReceiver() {
    	Log.debug(WePokerExtensionService.LOG_TAG, "extension receiver created");
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
    	Log.debug(WePokerExtensionService.LOG_TAG, "REceiver onReceive" + intent.getAction());
        intent.setClass(context, WePokerExtensionService.class);
        context.startService(intent);
    }
}