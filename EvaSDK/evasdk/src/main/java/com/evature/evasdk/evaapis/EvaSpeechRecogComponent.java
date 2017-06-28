package com.evature.evasdk.evaapis;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.evature.evasdk.R;
import com.evature.evasdk.util.DLog;

import java.util.concurrent.LinkedBlockingQueue;


/******
 * Thread with:
 * Streamer( = Recorder -> Encoder) -> Http
 * 
 * @author iftah
 */
public class EvaSpeechRecogComponent {
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
	private EvaHttpThread dictationTask=null;
	EvaVoiceClient mVoiceClient = null;
	Object cookie;

	EvaComponent  mEva;
	
	Context mContext;

	public EvaSpeechRecogComponent(Context context, EvaComponent eva) {
		mEva = eva;
		mContext = context;
		mSpeechAudioStreamer = new SpeechAudioStreamer(mContext, SAMPLE_RATE);
	}
	
	public EvaSpeechRecogComponent(EvaComponent eva) {
		this(eva.activity, eva);
	}
	
//	public EvaSpeechComponent(EvaBaseActivity evaActivity) {
//		this(evaActivity.eva);
//	}

	
	public interface SpeechRecognitionResultListener {
		// Exactly one of the following methods will be called (for each Speech Recognition call)
		
		/***
		 * @param evaJson - the response
         * @param debugData: contains debug info:
		 *           totalTimeRecording - from start of streaming to finish of response downloading
		 *           timeWaitingServer - time between upload of last chunk and initial download of response
		 *           timeSpentReadingResponse - from start of response to end of response
		 *           timeSpentUpload - from start of waiting for upload stream to the end of upload
		 */
		void speechResultOK(String evaJson, Bundle debugData, Object cookie);
		
		void speechResultError(String message, Object cookie);
	}
	
	public boolean isInSpeechRecognition() {
		return dictationTask != null && dictationTask.mListener != null;
	}
	
	private class EvaHttpThread extends AsyncTask<Object, Object, Object>
	{
		public SpeechRecognitionResultListener mListener;
		private EvaVoiceClient  mVoiceClient;
		
		EvaHttpThread(EvaVoiceClient voiceClient, SpeechRecognitionResultListener listener) {
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
 					mListener.speechResultError(mContext.getResources().getString(R.string.evature_network_error), cookie);
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

			mVoiceClient.cancelTransfer();

			if (isCancelled() == false) {
				mVoiceClient.startVoiceRequest();
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
		if (dictationTask != null) {
			dictationTask.mListener = null;
		}
		if (mSpeechAudioStreamer != null) {
			mSpeechAudioStreamer.stop();
		}
		if (mVoiceClient != null)
		{
			mVoiceClient.cancelTransfer();
		}
	}

	public void startRecognizer(SpeechRecognitionResultListener listener, Object cookie,
                                boolean editLastUtterance, String rid)  {
		DLog.d(TAG, "<<< Starting Speech Recognicition");
		this.cookie = cookie;
		// start thread
		LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
	
		// start a thread - reading from microphone to encoder to queue
		boolean success = mSpeechAudioStreamer.startStreaming(queue);
		if (!success) {
			DLog.e(TAG, "Failed getting audio content");
			return;
		}

		// start a thread - from queue to http connection
		mVoiceClient = new EvaVoiceClient(mContext, mEva, queue, editLastUtterance, rid);
		dictationTask = new EvaHttpThread(mVoiceClient, listener);
		dictationTask.execute((Object[])null);
	}
	
	public void onDestroy() {
		mSpeechAudioStreamer.onDestroy();
	}
}
