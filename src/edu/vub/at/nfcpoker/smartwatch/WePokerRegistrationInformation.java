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
