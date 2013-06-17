package com.evaapis;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.nuance.nmdp.speechkit.Recognition;

abstract public class SpeechRecognition {
	private final String TAG = SpeechRecognition.class.getSimpleName();
	Handler mHandler;
	
	protected Activity mParentActivity;
		
	public interface OnSpeechRecognitionResultsListerner
	{
		void onSpeechRecognitionResults(ArrayList<String> matches);
	}

	public SpeechRecognition(Activity parentActivity) {
		Log.d(TAG, "CTOR");
		mParentActivity = parentActivity;
		mHandler = new Handler(); // So other threads can call back this one
	}

	
	abstract protected void startVoiceRecognitionActivity(String mPreferedLanguage);
	
	public Dialog getListeningDialog() {
		return null;
	}

	
	void setResult(String result) {
		Log.d(TAG, "Got result: " + result);
		// EditText t = (EditText) findViewById(R.id.text_DictationResult);
		// if (t != null)
		// t.setText(result);
	}

	void setResults(Recognition.Result[] results) {
		Log.d(TAG, "Got x results = " + results.length);
		ArrayList<String> sentences = new ArrayList<String>();
		for (int i = 0; i < results.length; i++) { // This is ugly and I look down on Java!
			sentences.add(results[i].getText());
		}
		((EvaBaseActivity) mParentActivity).onSpeechRecognitionResults(sentences);

	}

	public void prepareDialog() {
		
		
	}
	
	/**
	 * This piece of code was used for preferences, it needs to go into app to allow it to decide which recognition toolkit to use:
	 * 
	 * 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.contains(prefGoogleValid)) { // Do this once per installation
			Log.d(TAG, "Creating GoogleSpeechRecognizer preference");
			Boolean valid = googleRecognizerExists(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(prefGoogleValid, valid);
			editor.commit(); // Commit the edits!
			if (valid) {
				Intent recognizerIntent = RecognizerIntent.getVoiceDetailsIntent(context);
				if (recognizerIntent != null) {
					context.sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(context), null,
							new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
				} else {
					Log.w(TAG, "Problem - got null from RecognizerIntent.getVoiceDetailsIntent()");
				}
			}
		}
		
		private final String prefGoogleValid = "GoogleSpeechRecognizer";
	
	
	 */
	
}