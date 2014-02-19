package com.evaapis.android;

import com.evaapis.R;
import com.evaapis.R.id;
import com.evaapis.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class EvaSpeechRecognitionActivity extends Activity implements EvaSpeechComponent.SpeechRecognitionResultListener {

	private static final String TAG = EvaSpeechRecognitionActivity.class.getSimpleName();;

	EvaSpeechComponent speechRecognition;

	private Button mStopButton;
	private TextView mLevel;
	private TextView mStatusText;
	private ProgressBar mProgressBar;
	private SoundLevelView mSoundView;

	
	Handler mUpdateLevel;

	private long mTimeActivityCreation;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		long t0 = System.nanoTime();
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		EvaComponent.EvaConfig config = (EvaComponent.EvaConfig)intent.getSerializableExtra("evaConfig");
		speechRecognition = new EvaSpeechComponent(this, config);
		
		Log.i(TAG,"Creating speech recognition activity");

		setContentView(R.layout.listening);
		mStopButton = (Button)findViewById(R.id.btn_listeningStop);
		mLevel = (TextView)findViewById(R.id.text_recordLevel);
		mStatusText = (TextView)findViewById(R.id.text_listeningStatus);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mSoundView = (SoundLevelView)findViewById(R.id.surfaceView_sound_wave);

		mStopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				speechRecognition.stop();
			}
		});
		
		mUpdateLevel = new Handler()  {
			@Override
			public void handleMessage(Message msg) {
				SpeechAudioStreamer  speechAudioStreamer = speechRecognition.getSpeechAudioStreamer();
				int level = speechAudioStreamer.getSoundLevel();
				mLevel.setText(""+level);
				if (speechAudioStreamer.wasNoise) {
					if (speechAudioStreamer.getIsRecording() == false) {
						mLevel.setText("");
						mStatusText.setText("Processing...");
						mProgressBar.setVisibility(View.VISIBLE);
					}
					else {
						mSoundView.setSoundData(
								speechAudioStreamer.getSoundLevelBuffer(), 
								speechAudioStreamer.getBufferIndex(),
								speechAudioStreamer.getPeakLevel(),
								speechAudioStreamer.getMinSoundLevel()
						);
						mSoundView.invalidate();
					}
				}
				sendEmptyMessageDelayed(0, 200);
				super.handleMessage(msg);
			}
		};
		
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		speechRecognition.start(this, null);
		mUpdateLevel.sendEmptyMessageDelayed(0, 100);
		mTimeActivityCreation = (System.nanoTime() - t0)/1000000;
	}
	
	@Override
	protected void onStop() {
		Log.i(TAG,"Stopping speech recognition activity");
		speechRecognition.stop();
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		mUpdateLevel.removeMessages(0);
		super.onDestroy();
	}

	@Override
	public void speechResultOK(String evaJson, Bundle debugData, Object cookie) {
		Intent intent = new Intent();

		intent.putExtra(EvaSpeechComponent.RESULT_EVA_REPLY, evaJson);
		intent.putExtras(debugData);
		intent.putExtra(EvaSpeechComponent.RESULT_TIME_ACTIVITY_CREATE, mTimeActivityCreation);
		
		setResult(RESULT_OK, intent);		
		Log.i(TAG,"<<< Finish speech recognition activity: OK");
		finish();
	}

	@Override
	public void speechResultError(String message, Object cookie) {
		Intent intent = new Intent();
		intent.putExtra(EvaSpeechComponent.RESULT_EVA_ERR_MESSAGE, message);
		setResult(RESULT_CANCELED, intent);
		Log.i(TAG,"<<< Finish speech recognition activity - on error:  "+message);
		finish();
	}


}