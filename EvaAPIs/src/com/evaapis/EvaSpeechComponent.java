package com.evaapis;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;



public class EvaSpeechComponent {
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;
	public static final String TAG = "EvaSpeechComponent";
	
	public static final String RESULT_TIME_ACTIVITY_CREATE = "TIME_CREATE";
	public static final String RESULT_TIME_RECORDING = "TIME_RECORDING";
	public static final String RESULT_TIME_SERVER = "TIME_SERVER";
	public static final String RESULT_TIME_RESPONSE = "TIME_RESPONSE";
	public static final String RESULT_TIME_EXECUTE = "TIME_EXECUTE";
	
	public static final String RESULT_EVA_REPLY = "EVA_REPLY";
	public static final String RESULT_EVA_ERR_MESSAGE = "EVA_ERR_MESSAGE";
	
	protected SpeechAudioStreamer mSpeechAudioStreamer;
	private EvaHttpDictationTask dictationTask=null;
	EvaVoiceClient mVoiceClient = null;
	Object cookie;

	EvaComponent.EvaConfig mConfig;
	
	Context mContext;
		
	public EvaSpeechComponent(Context context, EvaComponent.EvaConfig config) {
		mConfig = config;
		mContext = context;
	}
	
	public EvaSpeechComponent(EvaComponent eva) {
		this(eva.activity, eva.mConfig);
	}
	
	public EvaSpeechComponent(EvaBaseActivity evaActivity) {
		this(evaActivity.eva);
	}

	
	public interface SpeechRecognitionResultListener {
		// Exactly one of the following methods will be called (for each Speech Recognition call)
		
		/***
		 * @param evaJson - the response
		 * @param totalTimeRecording - from start of streaming to finish of response downloading
		 * @param timeWaitingServer - time between upload of last chunk and initial download of response
		 * @param timeSpentReadingResponse - from start of response to end of response
		 * @param timeSpentUpload - from start of waiting for upload stream to the end of upload
		 */
		void speechResultOK(String evaJson, Bundle debugData, Object cookie );
		
		void speechResultError(String message, Object cookie);
	}
	
	public boolean isInSpeechRecognition() {
		return dictationTask != null && dictationTask.mListener != null;
	}
	
	private class EvaHttpDictationTask extends AsyncTask
	{
		public SpeechRecognitionResultListener mListener;
		private EvaVoiceClient  mVoiceClient;
		
		EvaHttpDictationTask(EvaVoiceClient voiceClient, SpeechRecognitionResultListener listener) {
			mVoiceClient = voiceClient;
			mListener = listener;
		}
		

		@Override
		protected void onPostExecute(Object result) {
			String evaJson = mVoiceClient.getEvaResponse();		

			if(mListener==null) return;
			
			if((evaJson!=null) && (evaJson.length()!=0))
			{
				Bundle debugData = new Bundle();
				debugData.putLong(RESULT_TIME_RECORDING, mSpeechAudioStreamer.totalTimeRecording);
				debugData.putLong(RESULT_TIME_SERVER, mVoiceClient.timeWaitingForServer);
				debugData.putLong(RESULT_TIME_RESPONSE, mVoiceClient.timeSpentReadingResponse);
				debugData.putLong(RESULT_TIME_EXECUTE, mVoiceClient.timeSpentExecute);
				
				mListener.speechResultOK(evaJson, debugData, cookie); 
				
			}
			else
			{
 				if (mVoiceClient.hadError) {
 					mListener.speechResultError("There was an error contacting the server, please check your internet connection or try again later", cookie);
				}
				else {
					mListener.speechResultError("No result found", cookie);
				}
			}
			
			mListener = null;
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


	public SpeechAudioStreamer getSpeechAudioStreamer() {
		return mSpeechAudioStreamer;
	}


	public void stop() {
		mSpeechAudioStreamer.stop();
	}
	
	public void cancel() {
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
		
		dictationTask.mListener = null;
	}

	public void start(SpeechRecognitionResultListener listener, Object cookie) {
		this.cookie = cookie;
		mSpeechAudioStreamer = new SpeechAudioStreamer(mContext, SAMPLE_RATE);
		mVoiceClient = new EvaVoiceClient(mContext, mConfig, mSpeechAudioStreamer);
		mSpeechAudioStreamer.initRecorder();
		dictationTask = new EvaHttpDictationTask(mVoiceClient, listener);
		dictationTask.execute((Object[])null);
	}
}
