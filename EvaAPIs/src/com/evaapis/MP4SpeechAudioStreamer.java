package com.evaapis;

import java.io.IOException;
import java.io.InputStream;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.util.Log;

public class MP4SpeechAudioStreamer implements OnErrorListener
{
	public static final String TAG = "SpeechAudioStreamer";
	public static final int SAMPLE_RATE = 16000;

//	private static final long SILENCE_PERIOD = 2000;
//	private static final float SILENCE_THRESHOLD = 350;

	
	public MP4SpeechAudioStreamer(int sampleRate) throws Exception {
		
	}

	private StreamingLoop  mRecorderLoop; 
	private boolean mIsRecording;
//	private AudioRecord mRecorder;
	private MediaRecorder mRecorder;

	

	public void initRecorder() throws IllegalStateException, IOException {
		Log.i("EVA",">>>  Initializing recorder");
		mIsRecording = true;
		
//		int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        int minTargetSize = 4410 * 2;      // 0.1 seconds buffer size
//        if (minTargetSize < minBufferSize) {
//            minTargetSize = minBufferSize;
//        }

        if ( mRecorderLoop == null) {  
            mRecorderLoop = new StreamingLoop();
        }

        boolean success = mRecorderLoop.InitLoop(128, 8192);
        if (!success) {
        	Log.e(TAG, "Failed to initialize the recorder loop");
        }
        
		if (mRecorder == null) {
//            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                                        44100,
//                                        AudioFormat.CHANNEL_IN_MONO,
//                                        AudioFormat.ENCODING_PCM_16BIT,
//                                        minTargetSize);
        	mRecorder = new MediaRecorder();
        	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        	mRecorder.setOutputFile(mRecorderLoop.getSenderFileDescriptor());
        	mRecorder.setOnErrorListener(this);
        	mRecorder.prepare();
        }
		
		Log.i(TAG, "ready...");
    }

	void start()  {
		Log.i(TAG, "starting to stream...");
        mRecorder.start();
//        mRecorder.startRecording();
	}

	

	public void stop() {
		if (mIsRecording) {
			mIsRecording = false;
			Log.i(TAG, "Stopping");
			mRecorder.stop();
			Log.i(TAG, "Reseting");
			mRecorder.reset();
			Log.i(TAG, "Releasing");
			mRecorder.release();
			Log.i(TAG, "Releasing Loop");
			mRecorderLoop.ReleaseLoop();
			Log.i(TAG, "All done");
		}
	}



	public InputStream getInputStream() {
		try {
			return mRecorderLoop.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}



	public int getSoundLevel() {
		return mRecorder.getMaxAmplitude();
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {

		Log.e(TAG, "Error while recording what: "+what+"   extra: "+extra);
	}



}
