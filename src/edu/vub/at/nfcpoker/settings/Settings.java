package edu.vub.at.nfcpoker.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import edu.vub.at.nfcpoker.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;


public class Settings {
	public static volatile String UUID;
	public static volatile String NETWORK_GROUP;
	public static volatile String nickname;
	public static volatile int gamesPlayed;
	
	private static final int nicknameTxtLength = 10000;

	public static void loadSettings(Context ctx) {
		SharedPreferences settings = ctx.getSharedPreferences("settings", Activity.MODE_PRIVATE);
		UUID = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
		NETWORK_GROUP = "TODO-FROM-NFC";
		nickname = settings.getString("nickname", getRandomNickName(ctx));
		gamesPlayed = settings.getInt("gamesPlayed", 0);
	}

	public static void saveSettings(Context ctx) {
		SharedPreferences settings = ctx.getSharedPreferences("settings", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("nickname", nickname);
		editor.putInt("gamesPlayed", gamesPlayed);
		editor.commit();
	}

	private static String getRandomNickName(Context ctx) {
		InputStreamReader inputStream = new InputStreamReader(ctx.getResources().openRawResource(R.raw.random_names));
		BufferedReader br = new BufferedReader(inputStream);
		Random r = new Random();
		int desiredLine = r.nextInt(nicknameTxtLength);
		String theLine = "Player";
		int lineCtr = 0;
		try {
			while ((theLine = br.readLine()) != null)   {
				if (lineCtr == desiredLine) {
					break;
				}
				lineCtr++;
			}
		} catch (IOException e) { }
		return theLine;
	}
}
