package com.evaapis;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.speech.RecognizerIntent;

public class SpeechRecognitionGoogle extends SpeechRecognitionOpt {

	public static final int VOICE_RECOGNITION_REQUEST_CODE_GOOGLE = 1234; // Google
	
	public SpeechRecognitionGoogle(Activity parentActivity) {
		super(parentActivity);
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
