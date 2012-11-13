package com.evaapis;

import android.app.Activity;
import android.content.Context;

abstract public class SpeechRecognitionOpt implements SpeechRecognitionInterface {
	
	
	static String TAG = "SpeechRecognition";
	
	static public final int SPEECH_RECOGNITION_NUANCE = 1;
	static public final int SPEECH_RECOGNITION_GOOGLE = 2;

	static SpeechRecognitionOpt theVoiceEngine = null;

		
   Activity mParentActivity;
	
	public SpeechRecognitionOpt(Activity parentActivity) {
		mParentActivity = parentActivity;
	}

		
}
