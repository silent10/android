package com.evaapis;

import android.app.Activity;
import android.content.Intent;

public class SpeechRecognitionEva extends SpeechRecognition {

	public static final int VOICE_RECOGNITION_REQUEST_CODE_EVA = 0xBABE;

	public SpeechRecognitionEva(Activity parentActivity) {
		super(parentActivity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startVoiceRecognitionActivity(String mPreferedLanguage) {
		// Fire an intent to start the speech recognition activity.
		Intent intent = new Intent(mParentActivity.getApplicationContext(),EvaSpeechRecognitionActivity.class);
		mParentActivity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE_EVA);
		
	}

}
