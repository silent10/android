package com.evaapis;


import java.util.ArrayList;
import java.util.Locale;

import roboguice.activity.RoboFragmentActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.evaapis.SpeechRecognition.OnSpeechRecognitionResultsListerner;
import com.evature.util.ExternalIpAddressGetter;
import com.google.inject.Inject;
import com.google.inject.Injector;

abstract public class EvaBaseActivity extends RoboFragmentActivity implements OnSpeechRecognitionResultsListerner,EvaSearchReplyListener, OnInitListener{ 

	private String mPreferedLanguage = "en-US";	
	private String mLastLanguageUsed = "en-US";
	private final String TAG = "EvaBaseActivity";

	@Inject Injector injector;

	private boolean mTtsConfigured = false;
	private TextToSpeech mTts = null;
	protected SpeechRecognition mSpeechRecognition;

	@Inject private ExternalIpAddressGetter mExternalIpAddressGetter;
	@Inject private EvatureLocationUpdater mLocationUpdater;


	protected void speak(String sayIt) {
		if (mTts != null) {
			mTts.speak(sayIt, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	private void setTtsLanguage(String destLanguage) {
		// Set preferred language to whatever the user used to speak to phone.
		// Note that a language may not be available, and the result will indicate this.
		Locale aLocale = Locale.US; // new Locale(destLanguage.substring(0, 2), destLanguage.substring(3, 5));
		mTts.setLanguage(aLocale);
		 
	}

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
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
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String oldLanguage = new String(mPreferedLanguage);
		mPreferedLanguage = prefs.getString("languages", "en-US");
		if (!oldLanguage.equals(mPreferedLanguage)) // User changed the settings and chose a new speech recognition
			// language
			if (mTtsConfigured)
				setTtsLanguage(mPreferedLanguage);
		mLastLanguageUsed = new String(mPreferedLanguage);
	}

	
	
	
	// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == SpeechRecognitionEva.VOICE_RECOGNITION_REQUEST_CODE_EVA && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			
			String result = bundle.getString("EVA_REPLY");
			
			EvaApiReply apiReply = new EvaApiReply(result);		
			
			onEvaReply(apiReply);		
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	protected void onCreate(Bundle arg0) {
	
		if (mTts == null)
			mTtsConfigured = false;

		mTts = new TextToSpeech(this, this);
		mSpeechRecognition = new SpeechRecognitionEva(this);
		
		super.onCreate(arg0);
	}
	
	public void setPrefredLanguage(String preffredLanguage)
	{
		mPreferedLanguage = preffredLanguage;
	}
	
	public void searchWithVoice()
	{		
		mSpeechRecognition.startVoiceRecognitionActivity(mPreferedLanguage);
		mLastLanguageUsed = mPreferedLanguage;
	}

	public void searchWithText(String searchString)
	{
		EvaCallerTask callerTask = injector.getInstance(EvaCallerTask.class);
		callerTask.setListener(this);
		callerTask.execute(searchString, mLastLanguageUsed);
	}
	
	
	@Override
	public void onSpeechRecognitionResults(ArrayList<String> matches) {
		
		if (matches.size() > 0) {
			searchWithText(matches.get(0));	// priority speech parse
		}
	}



}
