package edu.vub.at.nfcpoker.smartwatch;

import android.os.Handler;

import com.esotericsoftware.minlog.Log;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

import android.os.Handler;
/**
 * The wePoker Extension Service handles registration and keeps track of
 * all controls on all accessories.
 */

public class WePokerExtensionService extends ExtensionService {

    public static final String LOG_TAG = "wePoker - watchExtension";
    
    public static final String EXTENSION_KEY = "com.sonyericsson.extras.liveware.extension.nfcpoker.key";

	public static final String INTENT_ACTION_START = "com.sonyericsson.extras.liveware.extension.nfcpoker.action.start";

    public WePokerExtensionService() {
        super(EXTENSION_KEY);
    }
    
    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.debug(WePokerExtensionService.LOG_TAG, " extension service onCreate");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new WePokerRegistrationInformation(this);
    }

    /*
     * (non-Javadoc)
     * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#
     * keepRunningWhenConnected()
     */
    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
    	 Log.debug(WePokerExtensionService.LOG_TAG, " createControlExtension with name " + hostAppPackageName);
        return new WePokerControlSmartWatch(hostAppPackageName, this, new Handler());
    }
    
}
