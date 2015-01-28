package com.evaapis.android;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.evaapis.EvaException;
import com.evature.util.DLog;


/******
 * Thread with:
 * Streamer( = Recorder -> Encoder) -> Http
 * 
 * @author iftah
 */
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

	EvaComponent  mEva;
	
	Context mContext;

	public EvaSpeechComponent(Context context, EvaComponent eva) {
		mEva = eva;
		mContext = context;
	}
	
	public EvaSpeechComponent(EvaComponent eva) {
		this(eva.activity, eva);
	}
	
//	public EvaSpeechComponent(EvaBaseActivity evaActivity) {
//		this(evaActivity.eva);
//	}

	
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
	
	private class EvaHttpDictationTask extends AsyncTask<Object, Object, Object>
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
				debugData.putLong(RESULT_TIME_EXECUTE, mVoiceClient.timeSpentUploading);
				
				mListener.speechResultOK(evaJson, debugData, cookie); 
				
			}
			else
			{
 				if (mVoiceClient.hadError) {
 					mListener.speechResultError(EvaComponent.NETWORK_ERROR, cookie);
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
				DLog.i(TAG, "<<< Waiting for previous transaction to complete");
				int count = 0;
				int MAX_WAIT_FOR_TRANSFER = 12 * 10; // 12 seconds max wait for finish of previous request
			
				while(mVoiceClient.getInTransaction()  && (count<MAX_WAIT_FOR_TRANSFER)) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						DLog.e(TAG, "Exception waiting for transfer to complete", e);
					}
					count++;
				}
			}

			mVoiceClient.stopTransfer();

			if (isCancelled() == false) {
				try {
					mVoiceClient.startVoiceRequest();
				} catch (Exception e) {
					if (e instanceof IOException) {
						IOException ioe = (IOException) e;
						if (ioe.getMessage().equals("Request aborted")) {
							DLog.w(TAG, "Request was aborted");
							return null;
						}
					}
					DLog.e(TAG, "Exception starting voice request", e);
				}
			}
			else {
				DLog.w(TAG, "Request was canceled");
			}
			return null;
		}

	}


	public SpeechAudioStreamer getSpeechAudioStreamer() {
		return mSpeechAudioStreamer;
	}


	public void stop() {
		if (mSpeechAudioStreamer != null)
			mSpeechAudioStreamer.stop();
	}
	
	public void cancel() {
		if (mSpeechAudioStreamer != null) {
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
	}

	public void start(SpeechRecognitionResultListener listener, Object cookie, boolean editLastUtterance) throws EvaException {
		this.cookie = cookie;
		mSpeechAudioStreamer = new SpeechAudioStreamer(mContext, SAMPLE_RATE);
		mSpeechAudioStreamer.initRecorder();
		mVoiceClient = new EvaVoiceClient(mContext, mEva, mSpeechAudioStreamer, editLastUtterance);
		dictationTask = new EvaHttpDictationTask(mVoiceClient, listener);
		dictationTask.execute((Object[])null);
	}
}
