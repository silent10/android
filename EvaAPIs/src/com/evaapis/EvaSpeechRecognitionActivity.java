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
import android.widget.TextView;
import android.widget.Toast;

public class EvaSpeechRecognitionActivity extends RoboActivity {

	protected static final String TAG = "EvaSpeechRecognitionActivity";

	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;
	public static final int SPEEX_MODE = 1;
	public static final int SPEEX_QUALITY = 8;

	
	private SpeechAudioStreamer mSpeechAudioStreamer;
	private EvaHttpDictationTask dictationTask;

	Button mStopButton;
	TextView mLevel;
	TextView mStatus;
	
	Handler mUpdateLevel;

	EvaVoiceClient mVoiceClient = null;

	private class EvaHttpDictationTask extends AsyncTask
	{
		
		public EvaSpeechRecognitionActivity mParent;
		private EvaVoiceClient  mVoiceClient;
		private boolean isDone = false;
		
		EvaHttpDictationTask(EvaVoiceClient voiceClient, EvaSpeechRecognitionActivity parent) {
			mVoiceClient = voiceClient;
			mParent = parent;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			isDone = true;
			Thread tr = new Thread()
			{
				public void run()
				{
					Log.i(TAG, "sending wav file");
					mVoiceClient.sendFile( mSpeechAudioStreamer.getWavFile() );
					Log.i(TAG, "Sent");
				}
			};
			tr.start();
			try {
				tr.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			String evaJson = mVoiceClient.getEvaJson();		

			if((evaJson!=null) && (evaJson.length()!=0))
			{
				Intent intent = new Intent();

				intent.putExtra("EVA_REPLY", evaJson);

				setResult(RESULT_OK, intent);
			}
			else
			{
				Toast.makeText(EvaSpeechRecognitionActivity.this, "No result found", Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED);
			}
			
			
			Log.i("EVA","Finish speech recognition activity");
			
			EvaSpeechRecognitionActivity.this.finish();
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			
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
			
			if(mVoiceClient.getInTransaction()) {
				mVoiceClient.stopTransfer();
			}

			mSpeechAudioStreamer.start();

			try {
				mSpeechAudioStreamer.waitForIt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		public int getSoundLevel() {
			if (isDone) {
				return -999;
			}
			return mSpeechAudioStreamer.getSoundLevel();
		}

	}


	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String sessionId = getIntent().getStringExtra("SessionId");
		
		Log.i(TAG,"Creating speech recognition activity");
		setContentView(R.layout.listening);

		mStopButton = (Button)findViewById(R.id.btn_listeningStop);
		mLevel=(TextView)findViewById(R.id.text_recordLevel);
		mStatus=(TextView)findViewById(R.id.text_listeningStatus);
		mStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "Stopping streamer");
				mSpeechAudioStreamer.stop();
			}
		});
		
		mUpdateLevel = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int level = dictationTask.getSoundLevel();
				if (level == -999) {
					mStatus.setText("Processing...");
					mLevel.setText("");
				}
				else {
					mLevel.setText(""+level);
					sendEmptyMessageDelayed(0, 100);
				}
				super.handleMessage(msg);
			}
		};
		
		String appKey = EvaAPIs.API_KEY;
		String siteCode = EvaAPIs.SITE_CODE;
		
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId=telephonyManager.getDeviceId();

		mVoiceClient = new EvaVoiceClient(siteCode, appKey, deviceId, sessionId);


		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		try {
			mSpeechAudioStreamer = new SpeechAudioStreamer(16000);  
			mSpeechAudioStreamer.initRecorder();
			
			dictationTask = new EvaHttpDictationTask(mVoiceClient, this);
			dictationTask.execute((Object[])null);
			mUpdateLevel.sendEmptyMessageDelayed(0, 100);
		
		} catch (Exception e) {
			setResult(RESULT_CANCELED);
			finish();
			e.printStackTrace();
		}

	}
	
	@Override
	protected void onStop() {
		Log.i("EVA","Stopping speech recognition activity");
//		mSpeechAudioStreamer.stop();
		if (mVoiceClient.getInTransaction())
		{
			Thread tr = new Thread()
			{
				public void run()
				{
//					mVoiceClient.endVoiceRequestFile();
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