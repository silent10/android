package com.evaapis.android;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.TelephonyManager;

import com.evaapis.android.EvaSpeechComponent.SpeechRecognitionResultListener;
import com.evaapis.crossplatform.EvaApiReply;
import com.evature.evaspeechrecognition.SpeechRecognition;
import com.evature.util.ExternalIpAddressGetter;
import com.evature.util.Log;



public class EvaComponent implements OnSharedPreferenceChangeListener, 
									SpeechRecognitionResultListener,
									EvaSearchReplyListener, OnInitListener{ 

	private static final int VOICE_RECOGNITION_REQUEST_CODE_EVA = 0xBABE;
	private static final int VOICE_RECOGNITION_REQUEST_CODE_GOOGLE = 0xBEEF;
	
	private final String TAG = "EvaComponent";
	

	public static final String DEBUG_PREF_KEY  = "eva_debug";
	public static final String LOCALE_PREF_KEY = "eva_preference_locale";
	public static final String LANG_PREF_KEY   = "eva_preference_language";
	public static final String VRSERV_PREF_KEY = "eva_vr_service";
	
	public static final String VPROXY_HOST_PREF_KEY = "eva_vproxy_host";
	public static final String WEBSERV_HOST_PREF_KEY = "eva_web_service_host";
	public static final String API_VER_PREF_KEY = "eva_api_ver";
	
	public static final String CONTEXT_FLIGHTS_PREF_KEY = "eva_context_flights"; 
	public static final String CONTEXT_HOTELS_PREF_KEY = "eva_context_hotels";  
	public static final String CONTEXT_VACATION_PREF_KEY = "eva_context_vacation";
	public static final String CONTEXT_CAR_PREF_KEY = "eva_context_car";
	public static final String CONTEXT_CRUISE_PREF_KEY = "eva_context_cruise";  
	public static final String CONTEXT_SKI_PREF_KEY = "eva_context_ski";     
	public static final String CONTEXT_EXPLORE_PREF_KEY = "eva_context_explore"; 


	// Eva Config
	EvaConfig mConfig;

	private String mLastLanguageUsed = "en";
	private boolean mDebug;
	
	
	private boolean mTtsConfigured = false;
	private TextToSpeech mTts = null;

	private ExternalIpAddressGetter mExternalIpAddressGetter;
	private EvatureLocationUpdater mLocationUpdater;
	
	Activity activity;
	private EvaSearchReplyListener replyListener;

	private static final String DefaultVProxyHost = "https://vproxy.evaws.com";
	private static final String DefaultEvaWSHost = "http://apiuseh.evaws.com";
	private static final String DefaultApiVersion = "v1.0";
	public static final String SDK_VERSION = "android_1.54";

	
	final static String NETWORK_ERROR = "There was an error contacting the server, please check your internet connection or try again later";
	
	/*****
	 * This class simplifies passing all the needed parameters down the levels of abstraction
	 */
	public static class EvaConfig implements Serializable {
		private static final long serialVersionUID = 1L;
		public String sessionId;
		public String appKey;
		public String siteCode;
		public String locale;// IL, UK or US - see docs
		public String language;
		public String appVersion;
		public String vrService;// voice recognition service
		public String deviceId;
		public String context;// "h" for hotels, "f" for flights, etc... see docs
		public String scope; // same values as context
		
		public String vproxyHost;
		public String webServiceHost;
		public String apiVersion;
		
		public EvaConfig() {
			sessionId = "1";
			language = "en";
			webServiceHost = DefaultEvaWSHost;
			vproxyHost = DefaultVProxyHost;
			apiVersion = DefaultApiVersion;
		}
	}
		
	
	public EvaComponent(Activity activity, EvaSearchReplyListener replyListener) {
		this(activity, replyListener, null);
		this.activity = activity;
		this.replyListener = replyListener;
		mLocationUpdater = new EvatureLocationUpdater();
		mExternalIpAddressGetter = new ExternalIpAddressGetter(mConfig.webServiceHost);
	}
	
	public EvaComponent(Activity activity, EvaSearchReplyListener replyListener, EvaConfig config) {
		if (config == null) {
			mConfig = new EvaConfig();
		}
		else {
			mConfig = config;
		}
		this.activity = activity;
		getDeviceId(); // fill the device Id in the mConfig
		this.replyListener = replyListener;
		mLocationUpdater = new EvatureLocationUpdater();
		mExternalIpAddressGetter = new ExternalIpAddressGetter(mConfig.webServiceHost);
	}
	
	public void speak(String sayIt) {
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

	public void onDestroy() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
	}
	
	public void onPause()
	{
		mExternalIpAddressGetter.pause();
		mLocationUpdater.stopGPS();
	}
	
	
	// Request updates at startupResults
	public void onResume() {
		mExternalIpAddressGetter.start();
		mLocationUpdater.startGPS();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i(TAG, "Preference "+key+" changed");
		setLocale(sharedPreferences.getString(LOCALE_PREF_KEY, "US"));
		setDebug(sharedPreferences.getBoolean(DEBUG_PREF_KEY, false));
		setVrService(sharedPreferences.getString(VRSERV_PREF_KEY, "none"));
		
		mConfig.vproxyHost = sharedPreferences.getString(VPROXY_HOST_PREF_KEY, "");
		if (mConfig.vproxyHost.equals("")) {
			Log.d(TAG, "Setting VProxy host to default");
			mConfig.vproxyHost =  DefaultVProxyHost;
		}
		mConfig.webServiceHost = sharedPreferences.getString(WEBSERV_HOST_PREF_KEY, "");
		if (mConfig.webServiceHost.equals("")) {
			Log.d(TAG, "Setting WebService host to default");
			mConfig.webServiceHost = DefaultEvaWSHost;
		}
		mConfig.apiVersion =  sharedPreferences.getString(API_VER_PREF_KEY, "");
		if (mConfig.apiVersion.equals("")) {
			Log.d(TAG, "Setting API Version host to default");
			mConfig.apiVersion = DefaultApiVersion;
		}

		String _context="";
		if (sharedPreferences.getBoolean(CONTEXT_FLIGHTS_PREF_KEY, false))   	_context += "f"; 
		if (sharedPreferences.getBoolean(CONTEXT_HOTELS_PREF_KEY, false))		_context += "h";  
		if (sharedPreferences.getBoolean(CONTEXT_VACATION_PREF_KEY, false))		_context += "v";
		if (sharedPreferences.getBoolean(CONTEXT_CAR_PREF_KEY, false))			_context += "c";
		if (sharedPreferences.getBoolean(CONTEXT_CRUISE_PREF_KEY, false))		_context += "r";
		if (sharedPreferences.getBoolean(CONTEXT_SKI_PREF_KEY, false))			_context += "s";
		if (sharedPreferences.getBoolean(CONTEXT_EXPLORE_PREF_KEY, false))		_context += "e";
		
		if ("".equals(_context)) {
			setContext(null);
		}		
		else {
			setContext(_context);
		}

		String oldLanguage = getPreferedLanguage();
		setPreferedLanguage(sharedPreferences.getString(LANG_PREF_KEY, "en"));
		
		if (!oldLanguage.equals(getPreferedLanguage())) {// User changed the settings and chose a new speech recognition
			// language
			if (mTtsConfigured) {
				Log.i(TAG, "Setting TTS language to "+mLastLanguageUsed);
				setTtsLanguage(getPreferedLanguage());
			}
		}
	}
	


	// this is the entry point for handling response - of a text-search request
	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {
		mEvaTextClient = null;
		if (reply.sessionId != null) {
			if (reply.sessionId.equals(getSessionId()) == false) {
				// not same as previous session = new session
				if ("1".equals(getSessionId()) == false) {
					replyListener.newSessionStarted(false);
				}
				setSessionId(reply.sessionId);
			}
		}
		else {
			if (reply.errorMessage != null) {
				Log.w(TAG, "Error from Eva: "+reply.errorMessage);
				onEvaError(reply.errorMessage, true, cookie);
				return;
			}
			
			// no session support - every reply starts a new session
			resetSession();
		}
		
		replyListener.onEvaReply(reply, cookie);
	}
	
	// handling errors
	@Override
	public void onEvaError(String message, boolean isServerError, Object cookie) {
		replyListener.onEvaError(message, isServerError, cookie);
	}
	
	// Handle the results from the speech recognition activity
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case VOICE_RECOGNITION_REQUEST_CODE_EVA:
				if (resultCode == Activity.RESULT_OK) {
					Log.i(TAG, "speech recognition activity result "+resultCode);
					Bundle bundle = data.getExtras();
					String evaJson = bundle.getString(EvaSpeechComponent.RESULT_EVA_REPLY);
					speechResultOK(evaJson, bundle, voiceActivityCookie);
				}
				else if (resultCode == Activity.RESULT_CANCELED) {
					String message;
					if (data == null) {
						message = "Canceled";
					}
					else {
						message = data.getStringExtra(EvaSpeechComponent.RESULT_EVA_ERR_MESSAGE);
					}
					speechResultError(message, voiceActivityCookie);
				}
				voiceActivityCookie = null;
				break;
				
			case VOICE_RECOGNITION_REQUEST_CODE_GOOGLE:
				// send the N-Best to VProxy for choice
				if (data != null && data.getExtras() != null) {
					Bundle bundle = data.getExtras();
					final Uri audioUri = data.getData();
					ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
					if (audioUri == null) {
						Log.d(TAG, "No voice file url");
						searchWithMultipleText(matches, voiceActivityCookie, false);
					}
					else {
						String recordingKey = SpeechRecognition.getRecordingKey(activity, data);
						Log.d(TAG, "Voice file url: "+audioUri);
						searchWithMultipleText(matches, voiceActivityCookie, false, recordingKey); 
					}
				}
				break;
		}
	}
	
	public void registerPreferenceListener() {
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this.activity);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.onSharedPreferenceChanged(prefs, "");
	}

	@Override
	public void speechResultOK(String evaJson, Bundle debugData, Object cookie) {
		
		EvaApiReply apiReply = new EvaApiReply(evaJson);
		if (mDebug && apiReply.JSONReply != null) {
			JSONObject debugJsonData = new JSONObject();
			try {
				debugJsonData.put("Time spent recording", debugData.getLong(EvaSpeechComponent.RESULT_TIME_RECORDING)+"ms");
				debugJsonData.put("Time in HTTP Execute", debugData.getLong(EvaSpeechComponent.RESULT_TIME_EXECUTE)+"ms");
				debugJsonData.put("Time spent server side", debugData.getLong(EvaSpeechComponent.RESULT_TIME_SERVER)+"ms");
				debugJsonData.put("Time reading HTTP response", debugData.getLong(EvaSpeechComponent.RESULT_TIME_RESPONSE)+"ms");
				if (debugJsonData.has(EvaSpeechComponent.RESULT_TIME_ACTIVITY_CREATE)) {
					debugJsonData.put("Time spent creating activity", debugData.getLong(EvaSpeechComponent.RESULT_TIME_ACTIVITY_CREATE)+"ms");					
				}
				apiReply.JSONReply.put("debug", debugJsonData);
			} catch (JSONException e) {
				Log.e(TAG, "Failed setting debug data", e);
			}
		}
		
		onEvaReply(apiReply, cookie);
	}

	@Override
	public void speechResultError(String message, Object cookie) {
		//Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		onEvaError(message, false, cookie);
	}
	
	public String getDeviceId() {
		if (mConfig.deviceId == null) {
			try {
				TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
				mConfig.deviceId=telephonyManager.getDeviceId();
			}
			catch(SecurityException e) {
				Log.i("EvaAPIs", "Can't get Device Id because missing READ_PHONE_STATE permission");
			}
		}
		return mConfig.deviceId;
	}
	
	

	public void onCreate(Bundle arg0) {
		EvatureLocationUpdater.initContext(activity);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		setDebug(sharedPreferences.getBoolean(DEBUG_PREF_KEY, false));
		setVrService(sharedPreferences.getString(VRSERV_PREF_KEY, "none")); 
		setPreferedLanguage(mLastLanguageUsed = sharedPreferences.getString(LANG_PREF_KEY, "en"));

		if (mTts == null)
			mTtsConfigured = false;
		mTts = new TextToSpeech(activity, this);
	}
	
	
	private Object voiceActivityCookie;
	private EvaTextClient mEvaTextClient;
	public void searchWithVoice(Object cookie)
	{
		// stop the TTS speech - so that we don't record the generated speech
		if (mTts != null) {
			mTts.stop();
		}
		mLastLanguageUsed = getPreferedLanguage();
		
		Log.i(TAG, "search with voice starting, lang="+getPreferedLanguage());

		Intent intent = new Intent(activity.getApplicationContext(), EvaSpeechRecognitionActivity.class);
		getDeviceId();
		intent.putExtra("evaConfig", mConfig);
		intent.putExtra("debug", mDebug);
		voiceActivityCookie = cookie;
		
		activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE_EVA);
	}
	
	public void searchWithLocalVoiceRecognition(int nbest, Object cookie) {
//		// Fire an intent to start the speech recognition activity.
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//		// Specify the calling package to identify your application (optional step)
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Travel Search Query");
		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// Specify how many results you want to receive. The results will be sorted
		// where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, nbest);
		// Specify the recognition language. This parameter has to be specified only if the
		// recognition has to be done in a specific language and not the default one (i.e., the system locale).
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getPreferedLanguage());
		
		SpeechRecognition.requestRecordingKey(intent);
		
		activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE_GOOGLE);
		voiceActivityCookie = cookie;
		
//		SpeechRecognizer recognizer = SpeechRecognizer
//	            .createSpeechRecognizer(activity.getApplicationContext());
//	    RecognitionListener listener = new RecognitionListener() {
//	        @Override
//	        public void onResults(Bundle results) {
//	            ArrayList<String> voiceResults = results
//	                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//	            if (voiceResults == null) {
//	                Log.e(TAG, "No voice results");
//	            } else {
//	                Log.d(TAG, "Printing matches: ");
//	                for (String match : voiceResults) {
//	                    Log.d(TAG, match);
//	                }
//	            }
//	        }
//
//	        @Override
//	        public void onReadyForSpeech(Bundle params) {
//	            Log.d(TAG, "Ready for speech");
//	        }
//
//	        @Override
//	        public void onError(int error) {
//	            Log.d(TAG,
//	                    "Error listening for speech: " + error);
//	        }
//
//	        @Override
//	        public void onBeginningOfSpeech() {
//	            Log.d(TAG, "Speech starting");
//	        }
//
//	        @Override
//	        public void onBufferReceived(byte[] buffer) {
//	        	Log.d(TAG, "Received buffer: "+buffer.length);
//	        }
//
//	        @Override
//	        public void onEndOfSpeech() {
//	        	Log.d(TAG, "end of speech");
//	        }
//
//	        @Override
//	        public void onEvent(int eventType, Bundle params) {
//	        	Log.d(TAG, "onEvent: "+eventType);
//	        }
//
//	        @Override
//	        public void onPartialResults(Bundle partialResults) {
//	            // TODO Auto-generated method stub
//
//	        }
//
//	        @Override
//	        public void onRmsChanged(float rmsdB) {
//	            Log.d(TAG, "onRms Change "+rmsdB);
//
//	        }
//	    };
//	    recognizer.setRecognitionListener(listener);
//	    recognizer.startListening(intent);
	}
	
	public void stopSearch() {
		if (mEvaTextClient != null) {
			mEvaTextClient.cancel(true);
			mEvaTextClient = null;
		}
	}
	
	public void searchWithText(String searchString, Object cookie, boolean editLastUtterance) {
		mLastLanguageUsed = getPreferedLanguage();
		Log.i(TAG, "search with text starting, lang="+mLastLanguageUsed);
		if (mEvaTextClient != null) {
			mEvaTextClient.cancel(true);
		}
		mEvaTextClient = new EvaTextClient(this, searchString, -1, cookie, editLastUtterance);
		mEvaTextClient.execute();
	}
	
	public void searchWithMultipleText(ArrayList<String> nBestTexts, Object cookie, boolean editLastUtterance) {
		mLastLanguageUsed = getPreferedLanguage();
		Log.i(TAG, "search with text starting, lang="+mLastLanguageUsed);
		if (mEvaTextClient != null) {
			mEvaTextClient.cancel(true);
		}
		mEvaTextClient = new EvaTextClient(this, nBestTexts, cookie, editLastUtterance);
		mEvaTextClient.execute();
	}
	

	public void searchWithMultipleText(ArrayList<String> nBestTexts, Object cookie, boolean editLastUtterance, String recordingKey) {
		mLastLanguageUsed = getPreferedLanguage();
		Log.i(TAG, "search with text starting, lang="+mLastLanguageUsed);
		if (mEvaTextClient != null) {
			mEvaTextClient.cancel(true);
		}
		mEvaTextClient = new EvaTextClient(this, nBestTexts, cookie, editLastUtterance);
		mEvaTextClient.setRecordingKey(recordingKey);
		mEvaTextClient.execute();
	}
	
	
	
	public void replyToDialog(int replyIndex) {
		replyToDialog(replyIndex, null);
	}
	
	public void replyToDialog(int replyIndex, Object cookie) {
		Log.i(TAG, "replying to dialog: "+replyIndex);
		if (mEvaTextClient != null) {
			mEvaTextClient.cancel(true);
		}
		mEvaTextClient = new EvaTextClient(this, null, replyIndex, cookie, false);
		mEvaTextClient.execute();
	}
	
	public boolean isNewSession() {
		return "1".equals(getSessionId());
	}
	
	public void resetSession() {
		setSessionId("1");
		replyListener.newSessionStarted(true);
	}

	@Override
	public void newSessionStarted(boolean selfTriggered) {
	}


	public boolean isDebug() {
		return mDebug;
	}

	public void setDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}

	public String getApiKey() {
		return mConfig.appKey;
	}

	public void setApiKey(String apiKey) {
		mConfig.appKey = apiKey;
	}

	public String getSiteCode() {
		return mConfig.siteCode;
	}

	public void setSiteCode(String siteCode) {
		mConfig.siteCode = siteCode;
	}

	public String getLocale() {
		return mConfig.locale;
	}

	public void setLocale(String locale) {
		mConfig.locale = locale;
	}

	public String getVrService() {
		return mConfig.vrService;
	}

	public void setVrService(String vrService) {
		mConfig.vrService = vrService;
	}

	public String getSessionId() {
		return mConfig.sessionId;
	}

	public String getPreferedLanguage() {
		return mConfig.language;
	}

	public void setPreferedLanguage(String preferedLanguage) {
		mConfig.language = preferedLanguage;
	}

	public void setSessionId(String sessionId) {
		mConfig.sessionId = sessionId;
	}


	public String getContext() {
		return mConfig.context;
	}
	
	public String getScope() {
		return mConfig.scope;
	}
	
	public void setContext(String context) {
		mConfig.context = context;
	}
	
	public void setScope(String scope) {
		mConfig.scope = scope;
	}
	
	public void setAppVersion(String ver) {
		mConfig.appVersion = ver;
	}
}
