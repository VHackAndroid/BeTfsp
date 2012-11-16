package edu.vub.at.nfcpoker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WePokerPreferencesActivity extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}