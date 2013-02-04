package com.evaapis;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

public class SpeechRecognitionNuance extends SpeechRecognition {

	private static final String TAG = SpeechRecognitionNuance.class.getSimpleName();
	private static SpeechKit mNuanceSpeechKit;
	private boolean mDestroyed; 
	protected ListeningDialog mListeningDialog; 
	protected Recognizer mCurrentRecognizer; 
	private final Recognizer.Listener mListener;
	
	public static final int LISTENING_DIALOG = 0; 
	
	private void createNuanceListeningDialog() {
		Log.d(TAG, "createNuanceListeningDialog()");
		mListeningDialog = new ListeningDialog(mParentActivity);
		mListeningDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mCurrentRecognizer != null) // Cancel the current recognizer
				{
					mCurrentRecognizer.cancel();
					mCurrentRecognizer = null;
				}

				if (!mDestroyed) {
					// Remove the dialog so that it will be recreated next time.
					// This is necessary to avoid a bug in Android >= 1.6 where the
					// animation stops working.
					mParentActivity.removeDialog(LISTENING_DIALOG);
					createNuanceListeningDialog();
				}
			}
		});
	}

	
	private void initNuance(Context context) {
		Log.d(TAG, "initNuance()");
		// Initialize the listening dialog
		createNuanceListeningDialog();
		mParentActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to
																			// parent activity
		mDestroyed = false;
		mNuanceSpeechKit = SpeechKit.initialize(context, NunaceKeys.SpeechKitAppId, NunaceKeys.SpeechKitServer,
				NunaceKeys.SpeechKitPort, NunaceKeys.SpeechKitSsl, NunaceKeys.SpeechKitApplicationKey);
		mNuanceSpeechKit.connect();
		// TODO: Keep an eye out for audio prompts not working on the Droid 2 or other 2.2 devices.
		Prompt beep = mNuanceSpeechKit.defineAudioPrompt(R.raw.beep);
		mNuanceSpeechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
	}
	
	public SpeechRecognitionNuance(Activity parentActivity) {
		super(parentActivity);
		Context context = parentActivity.getApplicationContext();
		initNuance(context);
		
		mListener = createListener(); // NuanceMainActivity
		mCurrentRecognizer = null;
		mDestroyed = true;
	}

	@Override
	public void startVoiceRecognitionActivity(String mPreferedLanguage) {
			Log.d(TAG, "startNuanceVoiceRecognitionActivity()");
			mListeningDialog.setText("Initializing...");
			mParentActivity.showDialog(LISTENING_DIALOG);
			mListeningDialog.setStoppable(false);
			mCurrentRecognizer = mNuanceSpeechKit.createRecognizer(Recognizer.RecognizerType.Dictation,
					Recognizer.EndOfSpeechDetection.Long, "en_US", mListener, mHandler);
			mCurrentRecognizer.start();
		
	}

	@Override
	public Dialog getListeningDialog() {
			return mListeningDialog;
	}

	@Override
	public void prepareDialog() {
		((ListeningDialog)getListeningDialog()).prepare(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Recognizer currentRecognizer = SpeechRecognitionNuance.this.get_currentRecognizer();
				if (currentRecognizer != null) {
					currentRecognizer.stopRecording();
				}
			}
		});
		
	}
	
	private Recognizer.Listener createListener() {
		return new Recognizer.Listener() {
			@Override
			public void onRecordingBegin(Recognizer recognizer) {
				mListeningDialog.setText("Recording...");
				mListeningDialog.setStoppable(true);
				mListeningDialog.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable() {
					public void run() {
						if (mListeningDialog != null && mListeningDialog.isRecording() && mCurrentRecognizer != null) {
							mListeningDialog.setLevel(Float.toString(mCurrentRecognizer.getAudioLevel()));
							mHandler.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) {
				mListeningDialog.setText("Processing...");
				mListeningDialog.setLevel("");
				mListeningDialog.setRecording(false);
				mListeningDialog.setStoppable(false);
			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) {
				if (recognizer != mCurrentRecognizer)
					return;
				if (mListeningDialog.isShowing())
					mParentActivity.dismissDialog(LISTENING_DIALOG);
				mCurrentRecognizer = null;
				mListeningDialog.setRecording(false);

				// Display the error + suggestion in the edit box
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();

				if (suggestion == null)
					suggestion = "";
				setResult(detail + "\n" + suggestion);
				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ mNuanceSpeechKit.getSessionId() + "]");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				if (mListeningDialog.isShowing())
					mParentActivity.dismissDialog(LISTENING_DIALOG);
				mCurrentRecognizer = null;
				mListeningDialog.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result[] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++) {
					rs[i] = results.getResult(i);
				}
				setResults(rs);
				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ mNuanceSpeechKit.getSessionId() + "]");
			}
		};
	}

	public Recognizer get_currentRecognizer() {
		return mCurrentRecognizer;
	}


}

	

