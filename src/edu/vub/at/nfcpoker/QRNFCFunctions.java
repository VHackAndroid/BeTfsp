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

package edu.vub.at.nfcpoker;

import java.nio.charset.Charset;

import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class QRNFCFunctions {

	// Last RFID Tag
	public static Tag lastSeenNFCTag;
	
	// Dialog box
	private static AlertDialog wifiDialog;
	
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

	public static String createJoinUri(String wifiGroupName, String wifiPassword, String ipAddress, int port, boolean isDedicated) {
		Uri uri = Uri.parse(Constants.INTENT_BASE_URL)
				.buildUpon()
				.appendQueryParameter(Constants.INTENT_WIFI_NAME, wifiGroupName)
				.appendQueryParameter(Constants.INTENT_WIFI_PASSWORD, wifiPassword)
				.appendQueryParameter(Constants.INTENT_SERVER_IP, ipAddress)
				.appendQueryParameter(Constants.INTENT_PORT, ""+port)
				.appendQueryParameter(Constants.INTENT_IS_DEDICATED, "" + true)
				.build();

		return uri.toString();
	}

	public static void showWifiConnectionDialog(final Activity act, final String wifiName, final String wifiPassword, final String ipAddress, final int port, boolean isDedicated) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		View dialogGuts = act.getLayoutInflater().inflate(R.layout.wifi_connection_dialog, null);
		
		TextView networkNameTV = (TextView) dialogGuts.findViewById(R.id.network_name);
		networkNameTV.setText(wifiName);
		TextView passwordTV = (TextView) dialogGuts.findViewById(R.id.password);
		passwordTV.setText(wifiPassword);
		
		Button nfcWriteButton = (Button) dialogGuts.findViewById(R.id.nfcwrite_btn);
		nfcWriteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				QRNFCFunctions.writeJoinInfoOnNFCTag(act, lastSeenNFCTag,
						QRNFCFunctions.createJoinUri(wifiName, wifiPassword, ipAddress, port, true));
			}
		});
		NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(act);
        if (mNfcAdapter == null) {
			nfcWriteButton.setEnabled(false);
		}
		

		Button dismissButton = (Button) dialogGuts.findViewById(R.id.dismiss_btn);
		dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiDialog != null) wifiDialog.dismiss();
			}
		});
        builder.setCancelable(true);
        
		try {
			String connectionString = QRNFCFunctions.createJoinUri(wifiName, wifiPassword, ipAddress, port, true);
			Bitmap qrCode = QRNFCFunctions.encodeBitmap(connectionString);
			ImageView qrCodeIV = (ImageView) dialogGuts.findViewById(R.id.qr_code);
			qrCodeIV.setImageBitmap(qrCode);
		} catch (WriterException e) {
			Log.e("wePoker - Server", "Could not create QR code", e);
		}
		
		wifiDialog = builder.setTitle("Connection details")
				       .setCancelable(true)
				       .setView(dialogGuts)
				       .create();
		wifiDialog.show();
	}

	private static NdefMessage stringToNdefMessage(String s) {
		NdefRecord r = new NdefRecord(
				NdefRecord.TNF_WELL_KNOWN, 
				NdefRecord.RTD_TEXT, 
				new byte[0], // No id.
				s.getBytes(Charset.forName("UTF-8")));
		return new NdefMessage(new NdefRecord[]{ r });
	}

	public static NdefMessage getServerInfoNdefMessage(String wifiGroupName, String wifiPassword, String ipAddress, int port, boolean isDedicated) {
		String uri = QRNFCFunctions.createJoinUri(wifiGroupName, wifiPassword, ipAddress, port, isDedicated);
		String s = uri.substring(Constants.INTENT_BASE_URL.length() - 1);
		NdefRecord r = new NdefRecord(
    			NdefRecord.TNF_WELL_KNOWN, 
    			NdefRecord.RTD_TEXT, 
    			new byte[0], // No id.
    			s.getBytes(Charset.forName("UTF-8")));
		Log.v("wePoker - NFC", "Beaming Uri: " + s);
        return new NdefMessage(new NdefRecord[]{ r });
	}

	public static Uri getUriFromNdefMessage(NdefMessage message) {
		Log.v("wePoker - NFC", "NDEFMSG received: " + Constants.INTENT_BASE_URL + new String((message.getRecords()[0]).getPayload()));
		return Uri.parse(Constants.INTENT_BASE_URL + new String((message.getRecords()[0]).getPayload()));
	}

	public static boolean writeJoinInfoOnNFCTag(Activity act, Tag tag, String info) {
		String s = info.substring(Constants.INTENT_BASE_URL.length() - 1);
		try {
			// First, try if the tag is an NDEF tag.
			Ndef ndef = Ndef.get(tag);
			NdefMessage msg = stringToNdefMessage(s);
			if (ndef == null) {
				NdefFormatable ndefFormatable = NdefFormatable.get(tag);
				ndefFormatable.connect();
				ndefFormatable.format(msg);
				ndefFormatable.close();
			} else {
				if (ndef.getMaxSize() < msg.toByteArray().length) {
					Log.e("wePoker - NFC", "NFC tag memory too small for: " + s);
					return false;
				} else {
					ndef.connect();
					ndef.writeNdefMessage(msg);
					ndef.close();
				}
			}
		} catch(Exception e) {
			Log.v("wePoker - NFC", "Could not write NFC tag ", e);
			try {
				Ndef ndef = Ndef.get(tag);
				if (ndef != null) { 
					ndef.close();
					return false;
				}
			} catch(Exception exc) {
				Log.wtf("wePoker - NFC", "Nothing to see here, move along.", exc);
				return false;
			}
		}
		return true;
	}

}
