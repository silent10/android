/**
 * 
 */
package com.evature.search.controllers.activities;

import java.util.Map;

import roboguice.activity.RoboPreferenceActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evature.search.R;

public class MyPreferences extends RoboPreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "MyPreferences";

	// This approach was deprecated, but the new fragments approach is not backwards compatible!!!
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences); // Yeah, I know, I know...
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup the initial summary values
		// Set up a listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i(TAG, "Preference "+key+" changed");
		Map<String, ?> allPref = sharedPreferences.getAll();
		for (Object k : allPref.keySet()) {
			Log.i(TAG, "Key: "+k+"  value: "+allPref.get(k));
		}
	}

}

// Remove a preference: http://stackoverflow.com/questions/2240326/remove-hide-a-preference-from-the-screen
