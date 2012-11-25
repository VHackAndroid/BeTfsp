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