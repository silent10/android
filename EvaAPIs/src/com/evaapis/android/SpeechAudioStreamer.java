package com.evaapis.android;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.evaapis.android.FLACStreamEncoder.WriteCallback;
import com.evature.util.DLog;


/***
 * Reads data of the microphone, encodes it and adds the encoded data to a Queue
 * @author iftah
 *
 */
public class SpeechAudioStreamer {
	public static final String TAG = "SpeechAudioStreamer";

	boolean mDebugSavePCM = true;
	
	// debug - time measurments
	public long totalTimeRecording;  // time from recording start to end (VAD detection)
	

	private AudioRecord mRecorder;
	private FLACStreamEncoder mEncoder;
	private byte[] mBuffer = null;
//	private int[] mBufferShorts = null;
	private boolean mIsRecording = false;

	// VAD Parameters,  default values
	private int mMovingAverageWindow = 5;		// volume level is average of last X chunks
	private long mPreVadRecordingTime = 150; 	// time from start of recording where no VAD is considered
	private long mNoisePeriod = 200;   			// minimum continuous noisy time to be considered "Noise"
	private long mSilencePeriod = 700; 			// minimum continuous silent time to be considered "Silence" 

	
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;

	private int mSampleRate;

	int mBufferIndex = 0;  // cyclic buffers index (cell used is:  index modulo buffer size)
	float mMovingAverageBuffer[]; // this is used to decide volume level  (eg. average of the last 5 buffers) 
	float mSoundLevelBuffer[] = new float[26]; // this is passed to the visualization of the sound-level

	long mStartLastSilence = -1;
	long mStartLastNoise = -1;
	private int mCurrentSoundLevel;
	private float mPeakSoundLevel;
	private float mMinSoundLevel;
	
	public boolean wasNoise;
	private int mRecorderBufferSize;



	public SpeechAudioStreamer(Context context, int sampleRate) {
//		fifoPath = context.getApplicationInfo().dataDir + "/flac_stream_fifo";

		mSampleRate = sampleRate;
		totalTimeRecording = 0;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mDebugSavePCM = prefs.getBoolean("eva_save_pcm", false);
//		mDebugSavePCM = false;
		
		
		mRecorderBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (mRecorderBufferSize <= 0) {
			DLog.e(TAG, "<<< bufferSize = "+mRecorderBufferSize);
			return;
		}
		mRecorderBufferSize *= 2; // for extra safety, do not loose audio even if delayed reading a bit
		mBuffer = new byte[mRecorderBufferSize];
		DLog.i(TAG, "<<< Initialized buffer to "+mRecorderBufferSize);
		
		mMovingAverageBuffer = new float[getMovingAverageWindow()];
	}

	private boolean initRecorder() {
		DLog.i(TAG, "<<< Initializing Recorder");
		
		wasNoise = false;
		mStartLastSilence = -1;
		mStartLastNoise = -1;
		mPeakSoundLevel = 0f;
		mMinSoundLevel = 999999f;
		if (mRecorder != null) {
			DLog.e(TAG, "<<< mRecorder initialized twice?");
			mRecorder.release();
		}
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				mRecorderBufferSize);
		
		DLog.d(TAG, "<<< Recorder state after init: "+mRecorder.getState());
		if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
			DLog.e(TAG, "<<< Failed to initalize recorder");
			mRecorder.release();
			mRecorder = null;
			return false;
		}
		return true;
	}
	
	
	public float[] getSoundLevelBuffer() {
		return mSoundLevelBuffer;
		//return mBufferShorts;
	}
	public int getBufferIndex() {
		//return Math.min(mBufferIndex, mBuffer.length) / 2;
		return mBufferIndex;
	}
	
	public int getPeakLevel() {
		return (int)mPeakSoundLevel;
	}
	public int getMinSoundLevel() {
		return (int)mMinSoundLevel;
	}
	
	public void addVolumeSample(float volumeSample) {

		mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ] =  volumeSample; // movingAverage;
		//Log.d(TAG, "@@@ added "+mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ]+"  to buffer, index now at "+mBufferIndex);
		mBufferIndex++;
	}

	float accumulator = 0;
	int samplesAccumulated = 0;
	
	private Boolean handleVolumeSample(float currentVolume, long startOfRecording) {

		mMovingAverageBuffer[mBufferIndex % mMovingAverageBuffer.length] = currentVolume;
		float movingAverage = 0.0f;
		int numOfVolumes = Math.min(mBufferIndex+1, mMovingAverageBuffer.length); 
		for (int i = 0; i < numOfVolumes; ++i)
			movingAverage += mMovingAverageBuffer[i];

		// Analyze movingAverage buffer.
		
		movingAverage /= numOfVolumes;
		this.mCurrentSoundLevel = (int) movingAverage;

		// reduce Peak and increase minSound - to allow temporary peak/min to disappear over time
//		float dist = mPeakSoundLevel - mMinSoundLevel;
//		float factor = dist * 0.01f;
//		mPeakSoundLevel -= factor;
//		mMinSoundLevel += factor;
		
		long timeRecording = (System.nanoTime() - startOfRecording) / 1000000;
		if (timeRecording < getPreVadRecordingTime()) {
			// too soon to consider VAD
			return null;
		}
//		
		if (movingAverage > mPeakSoundLevel) {
			mPeakSoundLevel = movingAverage;
		}
		if (movingAverage < mMinSoundLevel) {
			mMinSoundLevel = movingAverage;
		}
		
		addVolumeSample(currentVolume); // addVolumeSample(movingAverage);  
		
		// identifying speech start by sudden volume up in the last sample relative to the previous MOVING_AVERAGE_BUFFER_SIZE samples
//		if (mBufferIndex > MOVING_AVERAGE_BUFFER_SIZE && currentVolume > (2.0/MOVING_AVERAGE_BUFFER_SIZE) * movingAverage) {
//			// this last sample was half of the twice its part of last accumulation buffer
//			// this was a noise sample
//			return false;
//		}

		

		float volumeLevelFraction = ((float)(movingAverage - mMinSoundLevel))  / (mPeakSoundLevel-mMinSoundLevel);  
//		Log.i(TAG, "current D: "+(temp - mMinSoundLevel)+  "  peak D: "+(mPeakSoundLevel-mMinSoundLevel));
		if (volumeLevelFraction <=  0.20f) { // became silent
			mStartLastNoise = -1;
			
			// found start of silence ?
			if (mStartLastSilence == -1) {
				mStartLastSilence = System.currentTimeMillis();
				return null;
			}
			
			// long time of silence ?
			if ((System.currentTimeMillis() - mStartLastSilence) > getSilencePeriod()) {
				return true;
			}
			// not enough silent time passed
			return null;
		} 

		// this is not a silence chunk
		mStartLastSilence = -1;
		
		if (volumeLevelFraction >= 0.70f) {
			// this is a noise sequence
			
			// start of noise?
			if (mStartLastNoise == -1) {
				mStartLastNoise = System.currentTimeMillis();
				return null;
			}
			
			// long time noise?
			if ((System.currentTimeMillis() - mStartLastNoise) > getNoisePeriod()) {
				return false;
			}
			// not enough noisy time passed
			return null;
		}
		
		// this is not a noise chunk
		mStartLastNoise = -1;
		return null;
	}

	// called roughly every 50ms with 1600 bytes
	/***
	 * @param numberOfReadBytes
	 * @param startOfRecording
	 * @return  true when detected silence, false when detected noise, null when undetermined
	 */
	private void checkForSilence(int numberOfReadBytes, long startOfRecording) {

		if (numberOfReadBytes == 0)
			return;
		
		//Log.d(TAG, "Read "+numberOfReadBytes);
		
		if (mBuffer.length != numberOfReadBytes) {
			DLog.w(TAG, "<<< unexpected numread="+numberOfReadBytes+" but buffer has "+mBuffer.length);
			if (mBuffer.length < numberOfReadBytes) {
				numberOfReadBytes = mBuffer.length;
			}
		}
		
		int SAMPLES_PER_VOLUME = 50 * 16;  // 50ms at 16000 samples per second

		// Analyze Sound.
		float currentVolume = -1f;
		for (int i = 0; i < numberOfReadBytes; i += 2) {
			short sample = (short) ((mBuffer[i]) | mBuffer[i + 1] << 8);
			accumulator += sample * sample;
			samplesAccumulated++;
			if (samplesAccumulated >= SAMPLES_PER_VOLUME) {
				// volume: average of the current chunk
				currentVolume = accumulator / SAMPLES_PER_VOLUME;
				accumulator = 0.0f;
				samplesAccumulated = 0;
				
				Boolean hadSilence = handleVolumeSample(currentVolume, startOfRecording);
				
				if (!wasNoise && Boolean.FALSE == hadSilence) {
					DLog.d(TAG, "<<< Found initial noise");
					wasNoise = true;
				}
				if (wasNoise && Boolean.TRUE == hadSilence) {
					DLog.d(TAG, "<<< was noise and now silent - stopping recording");
					mIsRecording = false;
				}
			}
		}

	}

	public int getSoundLevel() {
		return mCurrentSoundLevel;
	}
	
	/****
	 * Read from Recorder and place in queue
	 */
	class Producer implements Runnable {
		private static final long MAX_TIME_WAIT_ITERATIONS = 300; // wait maximum 3 seconds
		private static final int MAX_ERROR_ITERATIONS = 5;
		DataOutputStream dos = null;
		FileOutputStream fos = null;
		FLACStreamEncoder encoder;
		final LinkedBlockingQueue<byte[]>  queue;

		Producer(FLACStreamEncoder encoder, final LinkedBlockingQueue<byte[]> queue) {
			this.encoder = encoder;
			this.queue = queue;
		}
		
		private void encode(byte[] chunk, int readSize) throws IOException {
			// long startTime = System.nanoTime();
			
			ByteBuffer bf = ByteBuffer.allocateDirect(readSize);
			bf.put(chunk, 0, readSize);
			
			//ByteBuffer bf = ByteBuffer.wrap(chunk);
			
			encoder.write(bf, readSize);
			//encoder.flush();
		}

		public void run() {
			long t0 = System.nanoTime();
			
			accumulator = 0.0f;
			samplesAccumulated = 0;
			
			DLog.d(TAG, "<<< Starting producer thread");

			if (mDebugSavePCM) {
				String fileBase = Environment.getExternalStorageDirectory()
						.getPath() + "/sample";
				File f = new File(fileBase + ".smp");

				try {
					fos = new FileOutputStream(f);
					dos = new DataOutputStream(new BufferedOutputStream(fos));
				} catch (FileNotFoundException e) {
					DLog.w(TAG, "<<< Failed to open file to save PCM");
				}
			}
			

			try {
				try {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					DLog.e(TAG, "<<< Set rec thread priority failed: " + e.getMessage());
				}
				

				for (int i = 0; i < mMovingAverageBuffer.length; i++) {
					mMovingAverageBuffer[i] = 0.0f;
				}
				for (int i=0; i<mSoundLevelBuffer.length; i++) {
					mSoundLevelBuffer[i] = 0;
				}
				
				mRecorder.startRecording();

				int iterations = 0;
				int errorIterations = 0;
				
				while (mIsRecording) {
					int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);

					iterations++;
					if (readSize < 0) {
						DLog.w(TAG, "<<< Error reading from recorder "+readSize);
						Thread.sleep(10);
						errorIterations++;
						if (errorIterations > MAX_ERROR_ITERATIONS) {
							DLog.e(TAG, "<<< Errors - quiting");
							break;
						}
					}
					else if (readSize == 0) {
						try {
							DLog.i(TAG, "<<< Waiting for microphone to produce data");
							Thread.sleep(10);
							if (iterations > MAX_TIME_WAIT_ITERATIONS) {
								DLog.e(TAG, "<<< Waited too long for microphone to produce data - quiting");
								break;
							}
						} catch (InterruptedException e) {
							DLog.e(TAG, "<<< Interruprted Producer stream", e);
						}
					} else {
						
						checkForSilence(readSize, t0);
						
						if (mIsRecording) {
							try {
								encode(mBuffer, readSize);
							} catch (IOException e) {
								DLog.e(TAG, "<<< IO error while sending to encoder");
							}
							
							if (mDebugSavePCM) {
								dos.write(mBuffer, 0, readSize);
							}
//							Thread.sleep(10);
						}
					}
				}
				encoder.flush();

				if (mDebugSavePCM) {
					dos.flush();
					dos.close();

					String fileBase = Environment.getExternalStorageDirectory().getPath() + "/sample";
					
					byte bytes[] = FileUtils.readFileToByteArray(new File( fileBase + ".smp"));

					WriteWavFile(new File(fileBase + ".wav"), 16000, bytes);
				}
				
//				queue.put(new byte[0]);

				DLog.i(TAG, "<<< Finished producer thread - iterations="+iterations+ "  time="+((System.nanoTime() - t0) / 1000000)+"ms");

			} catch (Exception ex) {
				DLog.e(TAG, "<<< Exception in microphone producer", ex);
			} finally {
				byte[] endOfRecording = new byte[0];
				queue.add(endOfRecording);
				mIsRecording = false;

				DLog.d(TAG, "<<< Releasing Recorder");
				if (mRecorder != null) {
					mRecorder.release();
					mRecorder = null;
				}
				DLog.d(TAG, "<<< Releasing Encoder");
				if (encoder != null) {
					encoder.release();
					encoder = null;
				}
				totalTimeRecording = (System.nanoTime() - t0) / 1000000;
				DLog.d(TAG, "<<< All done.");
			}
		}

	}


	public boolean startStreaming(final LinkedBlockingQueue<byte[]> queue) {
		if (mIsRecording) {
			DLog.w(TAG, "<<< Already recording - not starting 2nd time");
			return false;
		}
		DLog.d(TAG, "<<< Starting streaming");
		mBufferIndex = 0;

		boolean success = initRecorder();
		if (!success) {
			DLog.e(TAG, "<<< Failed initializing recorder - not recording");
			return false;
		}
		

		this.mEncoder = new FLACStreamEncoder();
		//mEncoder.init(fifoPath, mSampleRate, CHANNELS, 16, false, 256);

		mEncoder.setWriteCallback(new WriteCallback() {
			
			@Override
			public void onWrite(byte[] buffer, int length, int samples, int frame) {
				byte[] bufferCopy = new byte[length];
				System.arraycopy(buffer, 0, bufferCopy, 0, length);
				queue.add(bufferCopy);
			}
		});

		mIsRecording = true;
		mEncoder.initWithCallback( mSampleRate, CHANNELS, 16, false, 256);
		DLog.i(TAG, "<<< Encoder initialized " + mEncoder.toString());
		Producer p = new Producer(mEncoder, queue);
		
		new Thread(p).start();
		//new Thread(c).start();
		DLog.i(TAG, "<<< Audio Streamer started!");
		return true;
	}

	void WriteWavFile(File outputFile, int sampleRate, byte data[]) {
		long longSampleRate = sampleRate;
		int totalDataLen = data.length + 36;
		int totalAudioLen = data.length;
		long byteRate = longSampleRate * 2;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			DLog.e(TAG, "Exception opening file", e);
		}

		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) 1;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 1); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		try {
			out.write(header);
			out.write(data);
			out.flush();
			out.close();
		} catch (IOException e) {
			DLog.e(TAG, "IO exception saving wav file", e);
		}
	}

	public void stop() {
		mIsRecording = false;
	}
	
	public boolean getIsRecording() {
		return mIsRecording;
	}
	
	public void onDestroy() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
	}

	public int getMovingAverageWindow() {
		return mMovingAverageWindow;
	}

	public void setMovingAverageWindow(int mMovingAverageWindow) {
		this.mMovingAverageWindow = mMovingAverageWindow;
		if (mMovingAverageBuffer == null || mMovingAverageBuffer.length != mMovingAverageWindow) {
			mMovingAverageBuffer = new float[mMovingAverageWindow];
		}
	}

	public long getPreVadRecordingTime() {
		return mPreVadRecordingTime;
	}

	public void setPreVadRecordingTime(long mPreVadRecordingTime) {
		this.mPreVadRecordingTime = mPreVadRecordingTime;
	}

	public long getNoisePeriod() {
		return mNoisePeriod;
	}

	public void setNoisePeriod(long mNoisePeriod) {
		this.mNoisePeriod = mNoisePeriod;
	}

	public long getSilencePeriod() {
		return mSilencePeriod;
	}

	public void setSilencePeriod(long mSilencePeriod) {
		this.mSilencePeriod = mSilencePeriod;
	}
}
