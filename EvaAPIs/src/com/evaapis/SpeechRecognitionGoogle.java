package com.evaapis;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

public class SpeechRecognitionGoogle extends SpeechRecognition {

	private List<String> mGoogleLanguages = null;
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234; // Google
	public static final int VOICE_RECOGNITION_REQUEST_CODE_GOOGLE = 1234; // Google
	private static final String TAG = SpeechRecognitionGoogle.class.getSimpleName();
	
	public SpeechRecognitionGoogle(Activity parentActivity) {
		super(parentActivity);
	}
	
	private void updateSupportedLanguages(List<String> languages) {
		mGoogleLanguages = languages;
		Log.d(TAG, "updateSupportedLanguages()");
	}

	
	public List<String> getmGoogleLanguages() {
		return mGoogleLanguages;
	}
	
	private Boolean googleRecognizerExists(Context context) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities( // get all activities that support this intent
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			Log.d(TAG, "Google Recognizer exists!");
			return true;
		} else {
			Log.w(TAG, "Google Recognizer not present");
			// fatal_error(R.string.missing_recognizer);
			return false;
		}
	}
	
	/*
	 * Handles the response of the broadcast request about the recognizer supported languages. The receiver is required
	 * only if the application wants to do recognition in a specific language.
	 */
	private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, final Intent intent) {
			Log.d(TAG, "Receiving broadcast " + intent);
			final Bundle extra = getResultExtras(false);
			if (getResultCode() != Activity.RESULT_OK) {
				Log.w(TAG, "SupportedLanguageBroadcastReceiver onReceive Error code:" + getResultCode());
			}
			if (extra == null) {
				Log.w(TAG, "SupportedLanguageBroadcastReceiver onReceive: No extra");
			} else {
				if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateSupportedLanguages(extra
									.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
						}
					});
				}
				if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateLanguagePreference(extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
						}
					});
				}
			}
		}
	}
	

	private void updateLanguagePreference(String language) {
		// TextView textView = (TextView) findViewById(R.id.language_preference);
		// textView.setText(language);
		Log.d(TAG, "LanguagePreference = " + language);
	}



	@Override
	public void startVoiceRecognitionActivity(String mPreferedLanguage) {
		// Fire an intent to start the speech recognition activity.
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Specify the calling package to identify your application (optional step)
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Travel Search Query");
		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// Specify how many results you want to receive. The results will be sorted
		// where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
		// Specify the recognition language. This parameter has to be specified only if the
		// recognition has to be done in a specific language and not the default one (i.e., the system locale).
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mPreferedLanguage);
		mParentActivity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE_GOOGLE);
		
	}

	@Override
	public Dialog getListeningDialog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareDialog() {
		// TODO Auto-generated method stub
		
	}

}
