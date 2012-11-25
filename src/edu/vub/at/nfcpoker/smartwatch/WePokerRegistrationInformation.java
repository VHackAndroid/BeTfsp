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

import android.content.ContentValues;
import android.content.Context;

import com.esotericsoftware.minlog.Log;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

public class WePokerRegistrationInformation extends RegistrationInformation {
	
	final Context context;

    /**
     * Create control registration object
     *
     * @param context The context
     */

	public WePokerRegistrationInformation(Context c) {
		context = c;
	}
	
	// Following values for methods taken from code example 
	// com.sonyericsson.extras.liveware.extension.eight.puzzle;
	@Override
	public int getRequiredNotificationApiVersion() {
		return 0;
	}
	
	@Override
	public int getRequiredWidgetApiVersion() {
		return 0;
	}

	@Override
	public int getRequiredControlApiVersion() {
		return 1;
	}
	
	@Override
	public int getRequiredSensorApiVersion() {
		return 0;
	}
	
	@Override
	public ContentValues getExtensionRegistrationConfiguration() {
		   String iconHostApp = ExtensionUtils.getUriString(context, edu.vub.at.nfcpoker.R.drawable.ic_launcher);
	        String iconExtension = ExtensionUtils
	                .getUriString(context, edu.vub.at.nfcpoker.R.drawable.ic_launcher);

	        ContentValues values = new ContentValues();

	        values.put(Registration.ExtensionColumns.CONFIGURATION_TEXT,
	                context.getString(edu.vub.at.nfcpoker.R.string.app_name));
	        values.put(Registration.ExtensionColumns.NAME, context.getString(edu.vub.at.nfcpoker.R.string.extension_name));
	        values.put(Registration.ExtensionColumns.EXTENSION_KEY,
	                WePokerExtensionService.EXTENSION_KEY);
	        values.put(Registration.ExtensionColumns.HOST_APP_ICON_URI, iconHostApp);
	        values.put(Registration.ExtensionColumns.EXTENSION_ICON_URI, iconExtension);
	        values.put(Registration.ExtensionColumns.NOTIFICATION_API_VERSION,
	                getRequiredNotificationApiVersion());
	        values.put(Registration.ExtensionColumns.PACKAGE_NAME, context.getPackageName());
	        Log.debug(WePokerExtensionService.LOG_TAG, " getExtensionRegistrationConfiguration with name " + context.getPackageName());
	        return values;
	}
	// Original app had it, maybe not needed now
//	 @Override
//	    public boolean isDisplaySizeSupported(int width, int height) {
//	        return ((width == EightPuzzleControlSmartWatch.getSupportedControlWidth(mContext) && height == EightPuzzleControlSmartWatch
//	                .getSupportedControlHeight(mContext)));
//	    }

}
