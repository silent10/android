package com.softskills.evasearch;

import android.app.Application;
import android.util.Log;

import com.softskills.evasearch.database.EvaDatabase;

public class EvaSearchApplication extends Application {
	
	static EvaDatabase mEvaDb = null;
	
	static String mSecret = null;
	static String mApiKey = null;
	
	public static EvaDatabase getDb()
	{
		return mEvaDb;
	}
	
	@Override
	public void onCreate() {		
		Log.i("ESAPP","onCreate");
		
		mApiKey = getResources().getString(R.string.API_KEY);
		mSecret = getResources().getString(R.string.SECRET);
		super.onCreate();
	}
	
	static String getApiKey(){ return mApiKey;}
	static String getSecret(){ return mSecret;}

	@Override
	public void onLowMemory() {
		Log.i("ESAPP","onLowMemory");		
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		Log.i("ESAPP","OnTerminate");		
		super.onTerminate();
	}

	public static void setEvaDb(EvaDatabase db) {
		mEvaDb = db;
		
	}

}
