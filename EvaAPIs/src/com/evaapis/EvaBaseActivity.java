package com.evaapis;


import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.activity.RoboFragmentActivity;
import roboguice.event.EventManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.evaapis.events.NewSessionStarted;
import com.evature.util.ExternalIpAddressGetter;
import com.google.inject.Inject;
import com.google.inject.Injector;

abstract public class EvaBaseActivity extends RoboFragmentActivity 
									implements 
									OnSharedPreferenceChangeListener, 
									EvaSearchReplyListener, OnInitListener{ 

	private static final int VOICE_RECOGNITION_REQUEST_CODE_EVA = 0xBABE;
	protected String mSessionId = "1";
	private final String TAG = "EvaBaseActivity";
	private String mPreferedLanguage = "en";	
	private String mLastLanguageUsed = "en";

	protected static final String DEBUG_PREF_KEY = "debug";
	protected static final String LOCALE_PREF_KEY = "preference_locale";
	protected static final String LANG_PREF_KEY = "preference_language";
	protected static final String VRSERV_PREF_KEY = "vr_service";

	
	@Inject protected Injector injector;

	private boolean mTtsConfigured = false;
	private TextToSpeech mTts = null;

	@Inject private ExternalIpAddressGetter mExternalIpAddressGetter;
	@Inject private EvatureLocationUpdater mLocationUpdater;

	@Inject protected EventManager eventManager;
	protected boolean mDebug;
	protected String mVrService;
	private long startOfTextSearch;

	protected void speak(String sayIt) {
		if (mTts != null) {
			mTts.speak(sayIt, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	private void setTtsLanguage(String destLanguage) {
		// Set preferred language to whatever the user used to speak to phone.
		// Note that a language may not be available, and the result will indicate this.
		Locale aLocale = /*Locale.US*/ new Locale(destLanguage);
		mTts.setLanguage(aLocale);
		 
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			Log.i(TAG, "Setting TTS language to "+mLastLanguageUsed);
			setTtsLanguage(mLastLanguageUsed);
			// Check the documentation for other possible result codes.
			// For example, the language may be available for the locale, but not for the specified country and variant.
			mTtsConfigured = true;
		} else {
			// Initialization failed.
			mTts = null;
		}
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		mExternalIpAddressGetter.pause();
		mLocationUpdater.stopGPS();
	}
	
	
	// Request updates at startupResults
	@Override
	protected void onResume() {
		super.onResume();
		mExternalIpAddressGetter.start();
		mLocationUpdater.startGPS();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i(TAG, "Preference "+key+" changed");
		EvaAPIs.locale = sharedPreferences.getString(LOCALE_PREF_KEY, "US");
		if (DEBUG_PREF_KEY.equals(key)) {
			ActivityCompat.invalidateOptionsMenu(this);
		}
		mDebug = sharedPreferences.getBoolean(DEBUG_PREF_KEY, false);
		mVrService = sharedPreferences.getString(VRSERV_PREF_KEY, "none");

		String oldLanguage = mPreferedLanguage;
		mPreferedLanguage = sharedPreferences.getString(LANG_PREF_KEY, "en");
		if (!oldLanguage.equals(mPreferedLanguage)) // User changed the settings and chose a new speech recognition
			// language
			if (mTtsConfigured) {
				Log.i(TAG, "Setting TTS language to "+mLastLanguageUsed);
				setTtsLanguage(mPreferedLanguage);
			}
	}


	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {
		if (reply.sessionId != null) {
			if (reply.sessionId.equals(mSessionId) == false) {
				// not same as previous session = new session
				if ("1".equals(mSessionId) == false) {
					eventManager.fire(new NewSessionStarted() );
				}
				mSessionId = reply.sessionId;
			}
		}
		else {
			// no session support - every reply starts a new session
			resetSession();
		}
		
		if ("voice".equals(cookie) == false) {
			// coming from text search
			JSONObject debugData = new JSONObject();
			try {
				debugData.put("Time in HTTP Execute", ((System.nanoTime() - startOfTextSearch)/1000000)+"ms");
				reply.JSONReply.put("debug", debugData);
			} catch (JSONException e) {
				Log.e(TAG, "Failed setting debug data", e);
			}
		}
	}
	
	
	// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE_EVA && resultCode == RESULT_OK) {
			Log.i(TAG, "speech recognition activity result "+resultCode);
			Bundle bundle = data.getExtras();
			
			String result = bundle.getString(EvaSpeechRecognitionActivity.RESULT_EVA_REPLY);

			EvaApiReply apiReply = new EvaApiReply(result);
			if (mDebug && apiReply.JSONReply != null) {
				JSONObject debugData = new JSONObject();
				try {
					debugData.put("Time spent recording", bundle.getLong(EvaSpeechRecognitionActivity.RESULT_TIME_RECORDING)+"ms");
					debugData.put("Time spent creating activity", bundle.getLong(EvaSpeechRecognitionActivity.RESULT_TIME_ACTIVITY_CREATE)+"ms");
					debugData.put("Time in HTTP Execute", bundle.getLong(EvaSpeechRecognitionActivity.RESULT_TIME_EXECUTE)+"ms");
					debugData.put("Time spent server side", bundle.getLong(EvaSpeechRecognitionActivity.RESULT_TIME_SERVER)+"ms");
					debugData.put("Time reading HTTP response", bundle.getLong(EvaSpeechRecognitionActivity.RESULT_TIME_RESPONSE)+"ms");
					apiReply.JSONReply.put("debug", debugData);
				} catch (JSONException e) {
					Log.e(TAG, "Failed setting debug data", e);
				}
			}
			
			onEvaReply(apiReply, "voice");		
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	protected void onCreate(Bundle arg0) {
	

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mDebug = sharedPreferences.getBoolean(DEBUG_PREF_KEY, false);
		mVrService = sharedPreferences.getString(VRSERV_PREF_KEY, "none"); 
		mLastLanguageUsed = mPreferedLanguage = sharedPreferences.getString(LANG_PREF_KEY, "en");

		if (mTts == null)
			mTtsConfigured = false;
		mTts = new TextToSpeech(this, this);

		super.onCreate(arg0);
	}
	
	
	public void searchWithVoice()
	{
		// stop the TTS speech - so that we don't record the generated speech
		if (mTts != null) {
			mTts.stop();
		}
		mLastLanguageUsed = mPreferedLanguage;
		
		Log.i(TAG, "search with voice starting, lang="+mPreferedLanguage);

		Intent intent = new Intent(this.getApplicationContext(), EvaSpeechRecognitionActivity.class);
		intent.putExtra("SessionId", mSessionId);
		intent.putExtra("debug", mDebug);
		intent.putExtra("language", mPreferedLanguage);
		intent.putExtra("vr_service", mVrService);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE_EVA);

	}

	public void searchWithText(String searchString) {
		mLastLanguageUsed = mPreferedLanguage;
		Log.i(TAG, "search with text starting, lang="+mLastLanguageUsed);
		EvaCallerTask callerTask = injector.getInstance(EvaCallerTask.class);
		startOfTextSearch = System.nanoTime();
		callerTask.initialize(this, mSessionId, mLastLanguageUsed, searchString, -1, null);
		callerTask.execute();
	}
	
	public void replyToDialog(int replyIndex) {
		Log.i(TAG, "replying to dialog: "+replyIndex);
		EvaCallerTask callerTask = injector.getInstance(EvaCallerTask.class);
		callerTask.initialize(this, mSessionId, mLastLanguageUsed, null, replyIndex, null);
		callerTask.execute();
	}
	
	public boolean isNewSession() {
		return "1".equals(mSessionId);
	}
	
	public void resetSession() {
		mSessionId = "1";
		eventManager.fire(new NewSessionStarted() );
	}

}
