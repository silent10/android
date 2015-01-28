package com.evaapis.android;

import com.evaapis.EvaException;
import com.evaapis.android.EvaSpeechComponent.SpeechRecognitionResultListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;




abstract public class EvaBaseActivity extends Activity implements EvaSearchReplyListener, SpeechRecognitionResultListener { 

	protected EvaComponent eva;
	protected EvaSpeechComponent speechRecognition;
	
	public EvaBaseActivity() {
	}


	@Override
	public void onDestroy() {
		eva.onDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		eva.onPause();
	}
	
	
	// Request updates at startupResults
	@Override
	protected void onResume() {
		super.onResume();
		eva.onResume();
	}
	
// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		eva.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	protected void onCreate(Bundle arg0) {
		eva = new EvaComponent(this, this);
		eva.onCreate(arg0);
		speechRecognition = new EvaSpeechComponent(eva);
		super.onCreate(arg0);
	}
	
	
	// note: you should play a "beep" before calling this function to notify users a recording is starting
	//       also visual feedback is recommended
	public void searchWithVoice(Object cookie, boolean editLastUtterance)
	{
		try {
			speechRecognition.start(this, cookie, editLastUtterance);
		}
		catch (EvaException e) {
			speechResultError(e.getMessage(), cookie);
		}
	}
	
	// override to modify GUI - eg, enable microphone button
	public void speechResultOK(String evaJson, Bundle debugData, Object cookie ) {
		eva.speechResultOK(evaJson, debugData, cookie);
	}
	
	// override 
	public void speechResultError(String message, Object cookie) {
		eva.speechResultError(message, cookie);
	}


	public void searchWithText(String searchString, Object cookie, boolean editLastUtterance) {
		eva.searchWithText(searchString, cookie, editLastUtterance);
	}
	
	public void replyToDialog(int replyIndex) {
		eva.replyToDialog(replyIndex);
	}
	
	public void replyToDialog(int replyIndex, Object cookie) {
		eva.replyToDialog(replyIndex, cookie);
	}
	
	public boolean isNewSession() {
		return eva.isNewSession();
	}
	
	public void resetSession() {
		eva.resetSession();
	}
	
	public void cancelSearch() {
		if (speechRecognition.isInSpeechRecognition()) {
			speechRecognition.cancel();
		}
		else {
			eva.cancelSearch();
		}
	}
	
}
