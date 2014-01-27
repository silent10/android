package com.evature.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class EvaSettingsAPI {
	// WTF WTF WTF???
	public static String getCurrencyCode(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		int currencyIndex = Integer.parseInt(sp.getString("eva_preference_currency", "0"));
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
		int currencyIndex = Integer.parseInt(sp.getString("eva_preference_currency", "0"));
		String[] signs = context.getResources().getStringArray(R.array.currency_signs);

		String currencySymbol;
		if (currencyIndex < signs.length)
			currencySymbol = signs[currencyIndex];
		else
			currencySymbol = signs[0];

		return currencySymbol;
	}
	
	public static final String EVA_KEY = "eva_key";
	public static final String EVA_SITE_CODE = "eva_site_code";

	public static String getEvaKey(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String val= sp.getString(EVA_KEY, "");
		if (val.equals("")) {
			Resources resources = context.getResources();
			val = resources.getString(R.string.EVA_API_KEY);
			Editor edit = sp.edit();
			edit.putString(EVA_KEY, val);
			edit.commit();
		}
		return val;
	}
	
	public static String getEvaSiteCode(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String val = sp.getString(EVA_SITE_CODE, "");
		if (val.equals("")) {
			Resources resources = context.getResources();
			val = resources.getString(R.string.EVA_SITE_CODE);
			Editor edit = sp.edit();
			edit.putString(EVA_SITE_CODE, val);
			edit.commit();
		}
		return val;
	}
}
