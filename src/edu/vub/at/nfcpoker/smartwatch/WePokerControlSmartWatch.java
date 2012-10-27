package edu.vub.at.nfcpoker.smartwatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import edu.vub.at.nfcpoker.R;
import edu.vub.at.nfcpoker.ui.ClientActivity.ClientGameState;

public class WePokerControlSmartWatch extends ControlExtension {

	private Handler handler = null;
	
	private int width;
	private int height;
	private ClientGameState  gameState = ClientGameState.INIT;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    
	/**
     * Create wePoker control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     * @param handler The handler to use
     */
	public WePokerControlSmartWatch(final String hostAppPackageName, final Context context, Handler h) {
		super(context, hostAppPackageName);
		handler = h;
		width = getSupportedControlWidth(context);
		height = getSupportedControlHeight(context);  

	}
	
	 /**
     * Get supported control width.
     *
     * @param context The context.
     * @return the width.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(com.sonyericsson.extras.liveware.extension.util.R.dimen.smart_watch_control_width);
    }

    public void onStart() {
    	
     gameState = ClientGameState.PLAYING;
    }
    
    public void onResume() {
//        if (mGameState == GameState.FINISHED_SHOW_IMAGE
//                || mGameState == GameState.FINISHED_SHOW_MENU) {
//            // Redraw finished screen
//            mPressedActionImageId = 0;
//            mPressedActionDrawableId = 0;
//            mHandler.post(mDrawResult);
//        } else if (mGameTiles == null) {
//            startNewGame();
//        } else {
            drawLoadingScreen();
//            getCurrentImage(true);
//        }
    }
    /**
     * Get supported control height.
     *
     * @param context The context.
     * @return the height.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(com.sonyericsson.extras.liveware.extension.util.R.dimen.smart_watch_control_height);
    }

    @Override
    public void onTouch(final ControlTouchEvent event) {
        int action = event.getAction();
        if (gameState == ClientGameState.PLAYING) {
            if (action == Control.Intents.TOUCH_ACTION_PRESS) {
            	 // Create bitmap
                Bitmap menu = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
                TextView tw = new TextView(super.mContext);
                tw.setText("halloWorld");

                tw.measure(width, height);
                tw.layout(0, 0, tw.getMeasuredWidth(),
                		tw.getMeasuredHeight());
                // Set default density to avoid scaling.
                menu.setDensity(DisplayMetrics.DENSITY_DEFAULT);
                Canvas canvas = new Canvas(menu);
              //  canvas.drawBitmap(mCurrentImage, null, new Rect(0, 0, width, height), null);
                tw.draw(canvas);

                showBitmap(menu);
            }
        }
    }
    
           
    /**
     * Show loading image screen.
     */
    private void drawLoadingScreen() {
        // Draw loading
        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new LayoutParams(width, height));

        LinearLayout sampleLayout = (LinearLayout)LinearLayout.inflate(mContext, R.layout.activity_splash,
                root);
        sampleLayout.measure(width, height);
        sampleLayout
                .layout(0, 0, sampleLayout.getMeasuredWidth(), sampleLayout.getMeasuredHeight());
        // Create background bitmap for animation.
        Bitmap menu = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
        // Set default density to avoid scaling.
        menu.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        Canvas canvas = new Canvas(menu);
        sampleLayout.draw(canvas);

        showBitmap(menu);
    }


}
