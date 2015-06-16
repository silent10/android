package com.evature.evasdk.evaapis.android;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.evature.evasdk.util.DLog;

/****
 * TTS (Text To Speech) Helper
 * Allows setting onComplete callback per utterance, and automatically wait with speak until TTS is intialized
 *  
 * @author iftah
 */
public class EvaSpeak implements TextToSpeech.OnInitListener {

	protected static final String TAG = "EvaSpeak";

	private boolean mTtsConfigured = false;
	
	private TextToSpeech mTts;
	
	private int spokenUtteranceId = 0; // incremental id for each speak
	
	class PendingSayit {
		public String sayIt;
		public Runnable onComplete;
	}
	private ArrayList<PendingSayit> pendingSayit;  // say-it waiting while TTS is initializing
	
	private HashMap<String, Runnable>  onCompleteUtterance;

    private static EvaSpeak instance = null;
    public static EvaSpeak getOrCreateInstance(Context context) {
        if (instance == null) {
            instance = new EvaSpeak(context);
        }
        return instance;
    }

    public static EvaSpeak getInstance() {
        return instance;
    }

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public EvaSpeak(Context context) {
		mTts = new TextToSpeech(context, this);
		onCompleteUtterance = new HashMap<String, Runnable>();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				
				@Override
				public void onStart(String utteranceId) {}
				
				@Override
				public void onError(String utteranceId) {
					DLog.e(TAG, "Error playing utterance "+utteranceId);
					if (onCompleteUtterance.containsKey(utteranceId)) {
						onCompleteUtterance.remove(utteranceId);
					}
				}
				
				@Override
				public void onDone(String utteranceId) {
					if (onCompleteUtterance.containsKey(utteranceId)) {
						onCompleteUtterance.get(utteranceId).run();
						onCompleteUtterance.remove(utteranceId);
					}
				}
			});
		}
		else {
			mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
				@Override
				public void onUtteranceCompleted(String utteranceId) {
					if (onCompleteUtterance.containsKey(utteranceId)) {
						onCompleteUtterance.get(utteranceId).run();
						onCompleteUtterance.remove(utteranceId);
					}	
				}
			});
		}
		
	}

	public void stop() {
		onCompleteUtterance.clear();
		if (mTtsConfigured) {
			mTts.stop();
		}
		else if (pendingSayit != null) { 
			pendingSayit.clear();
		}
	}
	
	

	/***
	 * 
	 * @param sayIt
	 * @param flush
	 * @param onComplete
	 */
	public void speak(String sayIt, boolean flush, Runnable onComplete) {
		// TODO: check for errors, refactor to its own class
		if (mTts != null) {
			if (mTtsConfigured) {
				spokenUtteranceId++;
				HashMap<String, String> params = new HashMap<String, String>(1);
				final String utteranceId = String.valueOf(spokenUtteranceId);
				params.put(Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
				if (onComplete != null) {
					onCompleteUtterance.put(utteranceId, onComplete);
				}
				mTts.speak(sayIt, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, params);
			}
			else {
				if (pendingSayit == null) {
					pendingSayit = new ArrayList<PendingSayit>();
				}
				if (flush) {
					pendingSayit.clear();
				}
				PendingSayit pendingPair = new PendingSayit();
				pendingPair.sayIt = sayIt;
				pendingPair.onComplete = onComplete;
				pendingSayit.add(pendingPair);
			}
		}
	}
	
	private void setTtsLanguage(String destLanguage) {
		// Set preferred language to whatever the user used to speak to phone.
//		Locale aLocale = /*Locale.US*/ new Locale(destLanguage);
//		int result = mTts.setLanguage(aLocale);
//		if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//			mTts.setLanguage(Locale.US); // US English is always available on Android
//		}
//		Log.i(TAG, "Set TTS language to "+mTts.getLanguage());
	}
	

	// Implements TextToSpeech.OnInitListener.
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			//setTtsLanguage(getPreferedLanguage());
			mTtsConfigured = true;
			if (pendingSayit != null) {
				for (PendingSayit pair : pendingSayit) {
					speak(pair.sayIt, false, pair.onComplete);
				}
				pendingSayit = null;
			}
		} else {
			// Initialization failed.
			mTts = null;
			mTtsConfigured = false;
            DLog.w(TAG, "Text to Speech init failed: "+status);
		}
	}
	

	public void onDestroy() {
		// Now we changed TTS to be global instance, there is no need to destroy
		if (this != getInstance() && mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

	}
}
