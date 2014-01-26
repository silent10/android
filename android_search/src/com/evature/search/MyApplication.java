package com.evature.search;

// This class is needed so that application crashes are automatically reported back home.
// The formKey is a Google Docs key that enables the application to fill in an online Google "Excel" form.

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.evaapis.EvatureLocationUpdater;
import com.evature.search.models.expedia.EvaXpediaDatabase;
import com.evature.search.models.expedia.ExpediaRequestParameters;
import com.evature.search.models.travelport.EvaTravelportDatabase;
import com.evature.search.models.vayant.EvaVayantDatabase;

@ReportsCrashes(formKey = "dDk0dGxhc1B6Z05vaXh3Q0xxWnhnZlE6MQ")
public class MyApplication extends Application {

	static EvaXpediaDatabase mEvaDb = null;
	static ExpediaRequestParameters  requestParams = new ExpediaRequestParameters();
	static EvaVayantDatabase mVayantDb = null;
	static String mExpediaSecret = null;
	static String mExpediaApiKey = null;
	static String mExpediaClientId = null;
	private static final String TAG = "MyApplication";
	private static Context context; // http://stackoverflow.com/a/5114361/78234
	public static boolean AcraInitialized = false;

	@Override
	public void onCreate() {

		// if (!BuildConfig.DEBUG) // Not when in debug mode!
		// The following line triggers the initialization of ACRA
		ACRA.init(this); AcraInitialized = true;
		Log.d(TAG, "onCreate");

		Resources resources = getResources();
		mExpediaApiKey = resources.getString(R.string.EXPEDIA_API_KEY);
		mExpediaSecret = resources.getString(R.string.EXPEDIA_SECRET);
		mExpediaClientId = resources.getString(R.string.EXPEDIA_CLIENT_ID);
		
		EvatureLocationUpdater.initContext(this);
		
		super.onCreate();
		MyApplication.context = getApplicationContext();
	}

	public static EvaXpediaDatabase getDb() {
		return mEvaDb;
	}
	
	public static void setDb(EvaXpediaDatabase db) {
		mEvaDb = db;
	}
	
	public static ExpediaRequestParameters getExpediaRequestParams() {
		return requestParams;
	}

	public static String getExpediaApiKey() {
		return mExpediaApiKey;
	}

	public static String getExpediaSecret() {
		return mExpediaSecret;
	}

	public static String getExpediaClientId() {
		return mExpediaClientId;
	}

	@Override
	public void onLowMemory() {
		Log.e(TAG, "onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		Log.d(TAG, "OnTerminate");
		super.onTerminate();
	}

	// public static void setEvaDb(EvaDatabase db) {
	// mEvaDb = db;
	// }

	public static Context getAppContext() {
		return MyApplication.context;
	}

	public static EvaTravelportDatabase getFlightsDb() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static EvaVayantDatabase getJourneyDb() {
		if (mVayantDb == null) {
			mVayantDb = new EvaVayantDatabase();
		}
		return mVayantDb;
	}

}
