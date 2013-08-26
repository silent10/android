package com.evaapis;

import roboguice.activity.RoboActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class EvaSpeechRecognitionActivity extends RoboActivity {

	public static final String RESULT_TIME_ACTIVITY_CREATE = "TIME_CREATE";
	public static final String RESULT_TIME_RECORDING = "TIME_RECORDING";
	public static final String RESULT_TIME_SERVER = "TIME_SERVER";
	public static final String RESULT_TIME_RESPONSE = "TIME_RESPONSE";
	public static final String RESULT_TIME_EXECUTE = "TIME_EXECUTE";
	
	public static final String RESULT_EVA_REPLY = "EVA_REPLY";

	
	private static final String TAG = EvaSpeechRecognitionActivity.class.getSimpleName();;

	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;

	
	private SpeechAudioStreamer mSpeechAudioStreamer;
	private EvaHttpDictationTask dictationTask;

	private Button mStopButton;
	private TextView mLevel;
	private TextView mStatusText;
	private ProgressBar mProgressBar;
	private SoundLevelView mSoundView;

	
	Handler mUpdateLevel;

	EvaVoiceClient mVoiceClient = null;
	private long mTimeActivityCreation;
	private boolean mDebug;



	private class EvaHttpDictationTask extends AsyncTask
	{
		public EvaSpeechRecognitionActivity mParent;
		private EvaVoiceClient  mVoiceClient;
		
		EvaHttpDictationTask(EvaVoiceClient voiceClient, EvaSpeechRecognitionActivity parent) {
			mVoiceClient = voiceClient;
			mParent = parent;
		}
		

		@Override
		protected void onPostExecute(Object result) {
			String evaJson = mVoiceClient.getEvaResponse();		

			if(mParent==null) return;
			
			if((evaJson!=null) && (evaJson.length()!=0))
			{
				Intent intent = new Intent();

				intent.putExtra(RESULT_EVA_REPLY, evaJson);
				if (mDebug) {
					intent.putExtra(RESULT_TIME_RECORDING, mSpeechAudioStreamer.totalTimeRecording);
					intent.putExtra(RESULT_TIME_SERVER, mVoiceClient.timeWaitingForServer);
					intent.putExtra(RESULT_TIME_RESPONSE, mVoiceClient.timeSpentReadingResponse);
					intent.putExtra(RESULT_TIME_EXECUTE, mVoiceClient.timeSpentExecute);
					intent.putExtra(RESULT_TIME_ACTIVITY_CREATE, mTimeActivityCreation);
				}

				setResult(RESULT_OK, intent);
			}
			else
			{
 				if (mVoiceClient.hadError) {
					Toast.makeText(EvaSpeechRecognitionActivity.this, "There was an error contacting the server, please check your interenet connection or try again later", Toast.LENGTH_SHORT).show();
				}
				else {
	  				Toast.makeText(EvaSpeechRecognitionActivity.this, "No result found", Toast.LENGTH_SHORT).show();
				}
				setResult(RESULT_CANCELED);
			}
			Log.i(TAG,"<<< Finish speech recognition activity");
			
			finish();
			
			super.onPostExecute(result);
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			
			if (mVoiceClient.getInTransaction()) {
				Log.i(TAG, "<<< Waiting for previous transaction to complete");
				int count = 0;
				int MAX_WAIT_FOR_TRANSFER = 12 * 10; // 12 seconds max wait for finish of previous request
			
				while(mVoiceClient.getInTransaction()  && (count<MAX_WAIT_FOR_TRANSFER)) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					count++;
				}
			}

			mVoiceClient.stopTransfer();

			try {
				mVoiceClient.startVoiceRequest();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}
	


	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		long t0 = System.nanoTime();
		super.onCreate(savedInstanceState);
		
		String sessionId = getIntent().getStringExtra("SessionId");
		mDebug = getIntent().getBooleanExtra("debug", false);
		
		Log.i(TAG,"Creating speech recognition activity");

		setContentView(R.layout.listening);
		mStopButton = (Button)findViewById(R.id.btn_listeningStop);
		mLevel=(TextView)findViewById(R.id.text_recordLevel);
		mStatusText = (TextView)findViewById(R.id.text_listeningStatus);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mSoundView = (SoundLevelView)findViewById(R.id.surfaceView_sound_wave);

		mStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSpeechAudioStreamer.stop();
			}
		});
		
		mUpdateLevel = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int level = mSpeechAudioStreamer.getSoundLevel();
				mLevel.setText(""+level);
				if (mSpeechAudioStreamer.wasNoise) {
					if (mSpeechAudioStreamer.getIsRecording() == false) {
						mLevel.setText("");
						mStatusText.setText("Processing...");
						mProgressBar.setVisibility(View.VISIBLE);
					}
					else {
						mSoundView.setSoundData(
								mSpeechAudioStreamer.getSoundLevelBuffer(), 
								mSpeechAudioStreamer.getBufferIndex(),
								mSpeechAudioStreamer.getPeakLevel(),
								mSpeechAudioStreamer.getMinSoundLevel()
						);
						mSoundView.invalidate();
					}
				}
				sendEmptyMessageDelayed(0, 200);
				super.handleMessage(msg);
			}
		};
		
		String appKey = EvaAPIs.API_KEY;
		String siteCode = EvaAPIs.SITE_CODE;
		String locale = EvaAPIs.locale;

		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId=telephonyManager.getDeviceId();
		if (deviceId==null) {
			deviceId="none";
		}
		

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		try {
			mSpeechAudioStreamer = new SpeechAudioStreamer(this, SAMPLE_RATE);
			mVoiceClient = new EvaVoiceClient(siteCode, appKey, deviceId, sessionId, locale, mSpeechAudioStreamer);
			mSpeechAudioStreamer.initRecorder();
			dictationTask = new EvaHttpDictationTask(mVoiceClient, this);
			dictationTask.execute((Object[])null);
			mUpdateLevel.sendEmptyMessageDelayed(0, 100);
		} catch (Exception e) {
			setResult(RESULT_CANCELED);
			finish();
			e.printStackTrace();
		}
		mTimeActivityCreation = (System.nanoTime() - t0)/1000000;
	}
	
	@Override
	protected void onStop() {
		Log.i(TAG,"Stopping speech recognition activity");
		mSpeechAudioStreamer.stop();
		if (mVoiceClient.getInTransaction())
		{
			Thread tr = new Thread()
			{
				public void run()
				{
					mVoiceClient.stopTransfer();
				}
			};
			tr.start();
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		mUpdateLevel.removeMessages(0);
		dictationTask.mParent=null;
		super.onDestroy();
	}


}