package com.evaapis;


import java.util.ArrayList;
import java.util.Locale;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;

import com.evaapis.SpeechRecognition.OnSpeechRecognitionResultsListerner;

abstract public class EvaBaseActivity extends FragmentActivity implements OnSpeechRecognitionResultsListerner,EvaSearchReplyListener, OnInitListener{ 
	
	private String mPreferedLanguage = "en-US";	
	private String mLastLanguageUsed = "en-US";
	private final String TAB = "EvaBaseActivity";
	private boolean mTtsConfigured = false;
	private TextToSpeech mTts = null;
	static private String mExternalIpAddress = null;
	
	static public void setmExternalIpAddress(String mExternalIpAddress) {
		mExternalIpAddress = mExternalIpAddress;
	}

	static public String getmExternalIpAddress() {
		return mExternalIpAddress;
	}

	
	protected void speak(String say_it) {
		if (mTts != null) {
			mTts.speak(say_it, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	public void setTtsLanguage(String destLanguage) {
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
	
	// Request updates at startupResults
	@Override
	protected void onResume() {
		super.onResume();

		EvatureLocationUpdater.initContext(this.getApplicationContext());
		try {
			EvatureLocationUpdater location = EvatureLocationUpdater.getInstance();
			location.startGPS();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

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
			if (requestCode == SpeechRecognition.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				
				onSpeechRecognitionResults(bundle);
				
				
			}

			super.onActivityResult(requestCode, resultCode, data);
		}

	
	@Override
	protected void onCreate(Bundle arg0) {
		if (mSpeechRecognition == null) {
			mSpeechRecognition = new SpeechRecognition(this);
		}
		
		if (mTts == null)
			mTtsConfigured = false;

		mTts = new TextToSpeech(this, this);

		
		super.onCreate(arg0);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SpeechRecognition.LISTENING_DIALOG:
			return mSpeechRecognition.getListeningDialog();
		}
		return null;
	}

	
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch (id) {
		case SpeechRecognition.LISTENING_DIALOG:
			mSpeechRecognition.prepareDialog();
			break;
		}
	}

	protected SpeechRecognition mSpeechRecognition;
	
	void setPrefferedLanguage(String preffredLanguage)
	{
		mPreferedLanguage = preffredLanguage;
	}
	
	protected void searchWithVoice( )
	{		
		mSpeechRecognition.startVoiceRecognitionActivity(mPreferedLanguage);
		mLastLanguageUsed = mPreferedLanguage;
	}

	protected void searchWithText(String searchString)
	{
		new EvaCallerTask(this,this).execute(searchString, mLastLanguageUsed); // first item in "matches" is highest
		// priority speech parse
	}

	
	@Override
	public void onSpeechRecognitionResults(Bundle bundle) {
		ArrayList<String> matches =bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
		
		if (matches.size() > 0) {
			searchWithText(matches.get(0));
																				// priority speech parse
		}
	}

}
