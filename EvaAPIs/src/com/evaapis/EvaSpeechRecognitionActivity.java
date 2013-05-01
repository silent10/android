package com.evaapis;

import java.io.File;

import org.xiph.speex.SpeexEncoder;

import android.app.Activity;
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

public class EvaSpeechRecognitionActivity extends Activity {


	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;
	public static final int SPEEX_MODE = 1;
	public static final int SPEEX_QUALITY = 8;
	private AndroidRecording mRecorder;
	private SpeexEncoder mEncoder;

	private final String startRecordingLabel = "Start recording";
	private final String stopRecordingLabel = "Stop recording";
	private boolean mIsRecording = false;
	private File mRawFile;
	private File mEncodedFile;
	SpeechAudioStreamer mSpeechAudioStreamer;
	private EvaHttpDictationTask dictationTask;


	private class EvaHttpDictationTask extends AsyncTask
	{
		public EvaSpeechRecognitionActivity mParent;

		@Override
		protected void onPostExecute(Object result) {
			String evaJson = DictationHTTPClient.getEvaJson();		

			if(mParent==null) return;
			
			if((evaJson!=null) && (evaJson.length()!=0))
			{
				Intent intent = new Intent();

				intent.putExtra("EVA_REPLY", evaJson);

				setResult(RESULT_OK, intent);
			}
			else
			{
				Toast.makeText(EvaSpeechRecognitionActivity.this, "No result found", 1000).show();
				setResult(RESULT_CANCELED);
			}
			Log.i("EVA","Finish speech recognition activity");
			
			finish();
			
			super.onPostExecute(result);
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			String API_KEY = EvaAPIs.API_KEY;
			String SITE_CODE = EvaAPIs.SITE_CODE;
			
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			String DEVICE_ID=telephonyManager.getDeviceId();

			while(DictationHTTPClient.getInTransaction())
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				DictationHTTPClient.startDictation(mSpeechAudioStreamer,API_KEY,SITE_CODE,DEVICE_ID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	Button mStopButton;
	TextView mLevel;

	Handler mUpdateLevel = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			int level = DictationHTTPClient.getSoundLevel();
			mLevel.setText(""+level);
			sendEmptyMessageDelayed(0, 1000);
			super.handleMessage(msg);
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i("EVA","Creating speech recognition activity");
		setContentView(R.layout.listening);

		mStopButton = (Button)findViewById(R.id.btn_listeningStop);
		mLevel=(TextView)findViewById(R.id.text_recordLevel);

		mStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSpeechAudioStreamer.stop();
			}
		});

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		try {
			mSpeechAudioStreamer = new SpeechAudioStreamer(16000);
			mSpeechAudioStreamer.initRecorder();
			dictationTask = new EvaHttpDictationTask();
			dictationTask.mParent=this;
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
		mSpeechAudioStreamer.stop();
		if(DictationHTTPClient.getInTransaction())
		{
			Thread tr = new Thread()
			{
				public void run()
				{
					DictationHTTPClient.stopTransfer();
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