package com.evaapis;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

public class SpeechRecognition {
	private final String TAG = SpeechRecognition.class.getSimpleName();
	private static SpeechKit nuanceSpeechKit;
	private final String prefGoogleValid = "GoogleSpeechRecognizer";
	private Handler mHandler;
	private List<String> mGoogleLanguages = null;
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234; // Google
	private ListeningDialog _listeningDialog; // Nuance
	private Recognizer _currentRecognizer; // Nuance
	private Activity mParentActivity;
	public static final int LISTENING_DIALOG = 0; // Nuance
	private boolean _destroyed; // Nuance
	private final Recognizer.Listener _listener;
	
	public interface OnSpeechRecognitionResultsListerner
	{
		void onSpeechRecognitionResults(Bundle bundle);
	}

	public SpeechRecognition(Activity parentActivity) {
		Log.d(TAG, "CTOR");
		mParentActivity = parentActivity;
		mHandler = new Handler(); // So other threads can call back this one
		Context context = parentActivity.getApplicationContext();
		initNuance(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
		// if (prefs.getBoolean(prefGoogleValid, false)) {
		// initGoogle(context);
		// }
		_listener = createListener(); // NuanceMainActivity
		_currentRecognizer = null;
		_destroyed = true;
	}

	private void initNuance(Context context) {
		Log.d(TAG, "initNuance()");
		// Initialize the listening dialog
		createNuanceListeningDialog();
		mParentActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to
																			// parent activity
		_destroyed = false;
		nuanceSpeechKit = SpeechKit.initialize(context, NunaceKeys.SpeechKitAppId, NunaceKeys.SpeechKitServer,
				NunaceKeys.SpeechKitPort, NunaceKeys.SpeechKitSsl, NunaceKeys.SpeechKitApplicationKey);
		nuanceSpeechKit.connect();
		// TODO: Keep an eye out for audio prompts not working on the Droid 2 or other 2.2 devices.
		Prompt beep = nuanceSpeechKit.defineAudioPrompt(R.raw.beep);
		nuanceSpeechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
	}

	private void createNuanceListeningDialog() {
		Log.d(TAG, "createNuanceListeningDialog()");
		_listeningDialog = new ListeningDialog(mParentActivity);
		_listeningDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer != null) // Cancel the current recognizer
				{
					_currentRecognizer.cancel();
					_currentRecognizer = null;
				}

				if (!_destroyed) {
					// Remove the dialog so that it will be recreated next time.
					// This is necessary to avoid a bug in Android >= 1.6 where the
					// animation stops working.
					mParentActivity.removeDialog(LISTENING_DIALOG);
					createNuanceListeningDialog();
				}
			}
		});
	}

	// private void initGoogle(Context context) {
	// Log.d(TAG, "initGoogle()");
	// }

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

	private void updateSupportedLanguages(List<String> languages) {
		mGoogleLanguages = languages;
		Log.d(TAG, "updateSupportedLanguages()");
	}

	private void updateLanguagePreference(String language) {
		// TextView textView = (TextView) findViewById(R.id.language_preference);
		// textView.setText(language);
		Log.d(TAG, "LanguagePreference = " + language);
	}

	public List<String> getmGoogleLanguages() {
		return mGoogleLanguages;
	}

	public void startVoiceRecognitionActivity(String mPreferedLanguage) {
		// startGoogleVoiceRecognitionActivity(mPreferedLanguage);
		startNuanceVoiceRecognitionActivity();
	}

	private void startNuanceVoiceRecognitionActivity() {
		Log.d(TAG, "startNuanceVoiceRecognitionActivity()");
		_listeningDialog.setText("Initializing...");
		mParentActivity.showDialog(LISTENING_DIALOG);
		_listeningDialog.setStoppable(false);
		_currentRecognizer = nuanceSpeechKit.createRecognizer(Recognizer.RecognizerType.Dictation,
				Recognizer.EndOfSpeechDetection.Long, "en_US", _listener, mHandler);
		_currentRecognizer.start();
	}

	public void startGoogleVoiceRecognitionActivity(String mPreferedLanguage) {
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
		mParentActivity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	public ListeningDialog getListeningDialog() {
		return _listeningDialog;
	}

	public Recognizer get_currentRecognizer() {
		return _currentRecognizer;
	}

	private Recognizer.Listener createListener() {
		return new Recognizer.Listener() {
			@Override
			public void onRecordingBegin(Recognizer recognizer) {
				_listeningDialog.setText("Recording...");
				_listeningDialog.setStoppable(true);
				_listeningDialog.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable() {
					public void run() {
						if (_listeningDialog != null && _listeningDialog.isRecording() && _currentRecognizer != null) {
							_listeningDialog.setLevel(Float.toString(_currentRecognizer.getAudioLevel()));
							mHandler.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) {
				_listeningDialog.setText("Processing...");
				_listeningDialog.setLevel("");
				_listeningDialog.setRecording(false);
				_listeningDialog.setStoppable(false);
			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) {
				if (recognizer != _currentRecognizer)
					return;
				if (_listeningDialog.isShowing())
					mParentActivity.dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);

				// Display the error + suggestion in the edit box
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();

				if (suggestion == null)
					suggestion = "";
				setResult(detail + "\n" + suggestion);
				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ nuanceSpeechKit.getSessionId() + "]");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				if (_listeningDialog.isShowing())
					mParentActivity.dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result[] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++) {
					rs[i] = results.getResult(i);
				}
				setResults(rs);
				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ nuanceSpeechKit.getSessionId() + "]");
			}
		};
	}

	private void setResult(String result) {
		Log.d(TAG, "Got result: " + result);
		// EditText t = (EditText) findViewById(R.id.text_DictationResult);
		// if (t != null)
		// t.setText(result);
	}

	private void setResults(Recognition.Result[] results) {
		Log.d(TAG, "Got x results = " + results.length);
		Bundle a_bundle = new Bundle();
		ArrayList<String> sentences = new ArrayList<String>();
		for (int i = 0; i < results.length; i++) { // This is ugly and I look down on Java!
			sentences.add(results[i].getText());
		}
		a_bundle.putStringArrayList(RecognizerIntent.EXTRA_RESULTS, sentences);		
		((EvaBaseActivity) mParentActivity).onSpeechRecognitionResults(a_bundle);

	}

	public void prepareDialog() {
		getListeningDialog().prepare(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Recognizer currentRecognizer = SpeechRecognition.this.get_currentRecognizer();
				if (currentRecognizer != null) {
					currentRecognizer.stopRecording();
				}
			}
		});
		
	}
}