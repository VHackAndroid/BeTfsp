package edu.vub.at.nfcpoker;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class QRFunctions {

    // Taken from the ZXing source code.
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    
    public static Bitmap encodeBitmap(String contents) throws WriterException {
    	int width = 200;
    	int height = 200;
		BitMatrix result = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, width, height);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
          int offset = y * width;
          for (int x = 0; x < width; x++) {
            pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
          }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    
    public static String createJoinUri(String wifiGroupName, String wifiPassword, String ipAddress, boolean isDedicated) {
    	Uri uri = Uri.parse(Constants.INTENT_BASE_URL)
    			     .buildUpon()
    			     .appendQueryParameter(Constants.INTENT_WIFI_NAME, wifiGroupName)
    			     .appendQueryParameter(Constants.INTENT_WIFI_PASSWORD, wifiPassword)
    			     .appendQueryParameter(Constants.INTENT_SERVER_IP, ipAddress)
    			     .appendQueryParameter(Constants.INTENT_IS_DEDICATED, "" + true)
    			     .build();
    	
    	return uri.toString();
	}
    

	public static void showWifiConnectionDialog(Context ctx, String wifiName, String wifiPassword, String ipAddress, boolean isDedicated) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogGuts = inflater.inflate(R.layout.wifi_connection_dialog, null);
		
		TextView networkNameTV = (TextView) dialogGuts.findViewById(R.id.network_name);
		networkNameTV.setText(wifiName);
		TextView passwordTV = (TextView) dialogGuts.findViewById(R.id.password);
		passwordTV.setText(wifiPassword);
		builder.setCancelable(false);
		
		try {
			String connectionString = QRFunctions.createJoinUri(wifiName, wifiPassword, ipAddress, isDedicated);
			Bitmap qrCode = QRFunctions.encodeBitmap(connectionString);
			ImageView qrCodeIV = (ImageView) dialogGuts.findViewById(R.id.qr_code);
			qrCodeIV.setImageBitmap(qrCode);
		} catch (WriterException e) {
			Log.e("wePoker - Server", "Could not create QR code", e);
		}
		
		builder.setTitle("Connection details")
		       .setCancelable(true)
		       .setView(dialogGuts)
		       .show();
	}
}
