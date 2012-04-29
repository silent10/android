package com.softskills.evasearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EvaSettingsAPI {

	public static String getCurrencyCode(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String key = context.getResources().getString(R.string.key_currency_preference);
		
		int currencyIndex = Integer.parseInt(sp.getString(key, "0"));
		String[] entries = context.getResources().getStringArray(R.array.entries_currency_preference);
		
		String currencyCode;
		if (currencyIndex < entries.length)
			currencyCode = entries[currencyIndex];
		else
			currencyCode = entries[0];
		
		return currencyCode;
	}
	
	public static String getCurrencySymbol(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String key = context.getResources().getString(R.string.key_currency_preference);
		
		int currencyIndex = Integer.parseInt(sp.getString(key, "0"));
		String[] signs = context.getResources().getStringArray(R.array.currency_signs);
		
		String currencySymbol;
		if (currencyIndex < signs.length)
			currencySymbol = signs[currencyIndex];
		else
			currencySymbol = signs[0];
		
		return currencySymbol;
	}
	
}
