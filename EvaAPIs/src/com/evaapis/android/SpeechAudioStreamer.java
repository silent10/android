package com.evaapis.android;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.evaapis.EvaException;
import com.evature.util.Log;

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

	public static final int MOVING_AVERAGE_BUFFER_SIZE = 5;
	private static final long SILENCE_PERIOD = 700;
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;

	private static final int MAX_UPLOADER_WAIT_ITERATIONS = 300; // wait maximum 3 seconds

	private int mSampleRate;

	int mBufferIndex = 0;
	float mMovingAverageBuffer[] = new float[MOVING_AVERAGE_BUFFER_SIZE];
	float mSoundLevelBuffer[] = new float[26];

	long mLastStart = -1;
	private int mSoundLevel;
	private float mPeakSoundLevel;
	private float mMinSoundLevel;
	
	public boolean wasNoise;

	private String fifoPath;

	public SpeechAudioStreamer(Context context, int sampleRate) {
		fifoPath = context.getApplicationInfo().dataDir + "/flac_stream_fifo";

		this.mEncoder = new FLACStreamEncoder();
		mSampleRate = sampleRate;
		totalTimeRecording = 0;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mDebugSavePCM = prefs.getBoolean("eva_save_pcm", false);
//		mDebugSavePCM = false;
		
		Log.i(TAG, "Encoder=" + mEncoder.toString());
	}

	void initRecorder() throws EvaException {
		Log.i(TAG, "<<< Starting to record");
		
		wasNoise = false;
		mPeakSoundLevel = 0f;
		mMinSoundLevel = 999999f;
		if (mBuffer == null) {
			int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			if (bufferSize <= 0) {
				throw new EvaException("bufferSize = "+bufferSize);
			}
			bufferSize *= 1.25;
			mBuffer = new byte[bufferSize];
//			mBufferShorts = new int[bufferSize/2+1];
			Log.i(TAG, "Initialized buffer to "+bufferSize);
			mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
					bufferSize);
			if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
				Log.e(TAG, "Failed to initalize recorder");
			}
		}
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

	// called roughly every 50ms with 1600 bytes
	private Boolean checkForSilence(int numberOfReadBytes) {

		if (numberOfReadBytes == 0)
			return null;
		
		//Log.d(TAG, "Read "+numberOfReadBytes);
		
		if (mBuffer.length != numberOfReadBytes) {
			Log.w(TAG, "unexpected numread="+numberOfReadBytes+" but buffer has "+mBuffer.length);
			if (mBuffer.length < numberOfReadBytes) {
				numberOfReadBytes = mBuffer.length;
			}
		}

		// Analyze Sound.
		float currentVolume = 0.0f;
		for (int i = 0; i < numberOfReadBytes; i += 2) {
			short sample = (short) ((mBuffer[i]) | mBuffer[i + 1] << 8);
//			mBufferShorts[i/2] = sample;
			currentVolume += sample * sample;  // Math.abs(sample)
		}
		// volume: average of the current chunk
		currentVolume /= numberOfReadBytes;

		// Analyze movingAverage buffer.
		mMovingAverageBuffer[mBufferIndex % MOVING_AVERAGE_BUFFER_SIZE] = currentVolume;
		float movingAverage = 0.0f;
		for (int i = 0; i < MOVING_AVERAGE_BUFFER_SIZE; ++i)
			movingAverage += mMovingAverageBuffer[i];

		movingAverage /= MOVING_AVERAGE_BUFFER_SIZE;
		this.mSoundLevel = (int) movingAverage;

		// reduce Peak and increase minSound - to allow temporary peak/min to disappear over time
//		float dist = mPeakSoundLevel - mMinSoundLevel;
//		float factor = dist * 0.01f;
//		mPeakSoundLevel -= factor;
//		mMinSoundLevel += factor;
//		
		if (movingAverage > mPeakSoundLevel) {
			mPeakSoundLevel = movingAverage;
		}
		if (movingAverage < mMinSoundLevel) {
			mMinSoundLevel = movingAverage;
		}
		
		mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ] =  currentVolume; // movingAverage;
		//Log.d(TAG, "@@@ added "+mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ]+"  to buffer, index now at "+mBufferIndex);
		mBufferIndex++;
		
		// identifying speech start by sudden volume up in the last sample relative to the previous MOVING_AVERAGE_BUFFER_SIZE samples
//		if (mBufferIndex > MOVING_AVERAGE_BUFFER_SIZE && currentVolume > (2.0/MOVING_AVERAGE_BUFFER_SIZE) * movingAverage) {
//			// this last sample was half of the twice its part of last accumulation buffer
//			// this was a noise sample
//			return false;
//		}


//		Log.i(TAG, "current D: "+(temp - mMinSoundLevel)+  "  peak D: "+(mPeakSoundLevel-mMinSoundLevel));
		if ((movingAverage - mMinSoundLevel) <= (mPeakSoundLevel-mMinSoundLevel) * 0.20f) { // became silent
			if (mLastStart == -1) {
				mLastStart = System.currentTimeMillis();
				return null;
			} else {
				if ((System.currentTimeMillis() - mLastStart) > SILENCE_PERIOD) {
					// long time of silence
					return true;
				}
				// not enough time past
				return null;
			}
		} else {
			if (mLastStart != -1) {
				// noise again
				mLastStart = -1;
			}
		}

		return false;
	}

	public int getSoundLevel() {
		return mSoundLevel;
	}
	
	/****
	 * Read from Recorder and place in queue
	 */
	class Producer implements Runnable {
		private static final long MAX_TIME_WAIT_ITERATIONS = 300; // wait maximum 3 seconds
		private static final int MAX_ERROR_ITERATIONS = 5;
		DataOutputStream dos = null;
		FileOutputStream fos = null;

		Producer() {
		}
		
		private void encode(byte[] chunk, int readSize) throws IOException {
			//Log.i(TAG, "<<< encoding " + chunk.length + " bytes");

			// encoded = mEncoder.encode(chunk);

//			long startTime = System.nanoTime();
			
			ByteBuffer bf = ByteBuffer.allocateDirect(readSize);
			bf.put(chunk, 0, readSize);
			
			//ByteBuffer bf = ByteBuffer.wrap(chunk);
			
			mEncoder.write(bf, readSize);
//			long duration = (System.nanoTime() - startTime)/1000000;
//			Log.i(TAG, "<<< Writing "+chunk.length+" bytes to encoder took "+duration + " ms");
		}

		public void run() {
			long t0 = System.nanoTime();
			String fileBase = Environment.getExternalStorageDirectory()
					.getPath() + "/sample";

			if (mDebugSavePCM) {
				File f = new File(fileBase + ".smp");

				try {
					fos = new FileOutputStream(f);
					dos = new DataOutputStream(new BufferedOutputStream(fos));
				} catch (FileNotFoundException e) {
					Log.w(TAG, "Failed to open file to save PCM");
				}
			}
			

			try {

				for (int i = 0; i < MOVING_AVERAGE_BUFFER_SIZE; i++) {
					mMovingAverageBuffer[i] = 0.0f;
				}
				for (int i=0; i<mSoundLevelBuffer.length; i++) {
					mSoundLevelBuffer[i] = 0;
				}
				mRecorder.startRecording();

				try {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					Log.e(TAG,
							"Set rec thread priority failed: " + e.getMessage());
				}

				int iterations = 0;
				int errorIterations = 0;
				while (mIsRecording) {
					// int packetSize = 2 * CHANNELS *
					// mEncoder.speexEncoder.getFrameSize();
					int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
					

					// Log.i("EVA","Read:"+readSize);

					iterations++;
					if (readSize < 0) {
						Log.w(TAG, "Error reading from recorder "+readSize);
						Thread.sleep(10);
						errorIterations++;
						if (errorIterations > MAX_ERROR_ITERATIONS) {
							Log.e(TAG, "Errors - quiting");
							break;
						}
					}
					else if (readSize == 0) {
						try {
							Log.i(TAG, "Waiting for microphone to produce data");
							Thread.sleep(10);
							if (iterations > MAX_TIME_WAIT_ITERATIONS) {
								Log.e(TAG, "Waited too long for microphone to produce data - quiting");
								break;
							}
						} catch (InterruptedException e) {
							Log.e(TAG, "Interruprted Producer stream", e);
						}
					} else {
						Boolean hadSilence = checkForSilence(readSize);
						if (!wasNoise && Boolean.FALSE == hadSilence) {
							Log.i(TAG, "<<< Found initial noise");
							wasNoise = true;
						}
						if (wasNoise && Boolean.TRUE == hadSilence) {
							mIsRecording = false;
						} else {
							try {
								encode(mBuffer, readSize);
							} catch (IOException e) {
								Log.e(TAG, "IO error while sending to encoder");
							}
							
							if (mDebugSavePCM) {
								dos.write(mBuffer, 0, readSize);
							}

//							Thread.sleep(10);
						}
					}

				}

				mEncoder.flush();
				mEncoder.release();
				
				if (mDebugSavePCM) {
					dos.flush();
					dos.close();

					byte bytes[] = FileUtils.readFileToByteArray(new File(
							fileBase + ".smp"));

					WriteWavFile(new File(fileBase + ".wav"), 16000, bytes);
				}
				
//				queue.put(new byte[0]);

				Log.i(TAG, "<<< Finished producer thread - iterations="+iterations+ "  time="+((System.nanoTime() - t0) / 1000000)+"ms");

			} catch (Exception ex) {
				Log.e(TAG, "Exception in microphone producer", ex);
			} finally {
				if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
					mRecorder.stop();
				}
				// mRecorder is initalized only once!
//				mRecorder.release();
//				mRecorder = null;
				totalTimeRecording = (System.nanoTime() - t0) / 1000000;
			}
		}

	}


	/***
	 * Read from encoder Fifo and write to HTTP stream
	 */
	class Uploader implements Runnable {
		boolean startedReading  = false;
		InputStream encodedStream = null;
		
		@Override
		public void run() {
//			String fileBase = Environment.getExternalStorageDirectory()
//					.getPath() + "/"+FILENAME;

//			DataOutputStream dos = null;
//			
//			if (mDebugSaveEncoded) {
//				File f = new File(fileBase + ".flac");
//
//				try {
//					FileOutputStream fos = new FileOutputStream(f);
//					dos = new DataOutputStream(new BufferedOutputStream(fos));
//				} catch (FileNotFoundException e) {
//					Log.e(TAG, "Failed to open debug file",e);
//				}
//			}
//
//	
			encodedStream = null;
			Log.i(TAG, "initing Fifo");
			mEncoder.initFifo(fifoPath);
			try {
				startedReading = true; // must be set before encodedStream, since the FileInputStream will be stuck until someone reads from it
				encodedStream = new FileInputStream(fifoPath);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Failed to open FIFO from encoder");
				throw new RuntimeException("Failed to open FIFO from encoder");
			}
			Log.i(TAG, "Encoded stream ready");
			
		}

	}

	public InputStream start() {
		if (mIsRecording) {
			Log.w(TAG, "Already recording");
			return null;
		}
		mBufferIndex = 0;
		mIsRecording = true;
//		BlockingQueue q = new ArrayBlockingQueue<byte[]>(1000);
		Producer p = new Producer();
		//	Consumer c = new Consumer(q);
		Uploader u = new Uploader();
		Thread uploaderThread = new Thread(u);
		uploaderThread.start(); // start uploader first -
								// must read from encoded FIFO before any write takes place
		int iterations = 0;
		while (u.startedReading == false) {
			iterations++;
			try {
				Log.i(TAG, "Waiting for uploader to start reading from FIFO before writing");
				Thread.sleep(10);
				if (iterations > MAX_UPLOADER_WAIT_ITERATIONS) {
					Log.e(TAG, "Waited too long for uploader to start reading");
					uploaderThread.interrupt();
					return null;
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "interrupted waiting for uploader", e);
				return null;
			}
		}

		Log.i(TAG, "Encoder init");
		mEncoder.init(fifoPath, mSampleRate, CHANNELS, 16); // write header

		new Thread(p).start();
		//new Thread(c).start();
		Log.i(TAG, "Audio Streamer started!");
		
		while (u.encodedStream == null) {
			iterations++;
			try {
				Log.i(TAG, "Waiting for uploader to start reading from FIFO before writing 2");
				Thread.sleep(10);
				if (iterations > MAX_UPLOADER_WAIT_ITERATIONS) {
					Log.e(TAG, "Waited too long for uploader to start reading 2");
					uploaderThread.interrupt();
					return null;
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "interrupted waiting for uploader", e);
				return null;
			}
		}
		
		return u.encodedStream;
	}

	/*
	 * private void encodeFile(final File inputFile, final File outputFile)
	 * throws IOException {
	 * 
	 * FLACStreamEncoder encoder = new FLACStreamEncoder(); encoder.(SPEEX_MODE,
	 * SPEEX_QUALITY, SAMPLE_RATE, CHANNELS);
	 * 
	 * DataInputStream input = null; AudioFileWriter output = null; try { input
	 * = new DataInputStream(new FileInputStream(inputFile)); output = new
	 * OggSpeexWriter(SPEEX_MODE, SAMPLE_RATE, CHANNELS, 1, false);
	 * output.open(outputFile); output.writeHeader("Encoded with: " +
	 * SpeexEncoder.VERSION);
	 * 
	 * byte[] buffer = new byte[2560]; // 2560 is the maximum needed value
	 * (stereo UWB) int packetSize = 2 * CHANNELS * encoder.getFrameSize();
	 * 
	 * while (true) { input.readFully(buffer, 0, packetSize);
	 * encoder.processData(buffer, 0, packetSize); int encodedBytes =
	 * encoder.getProcessedData(buffer, 0); if (encodedBytes > 0) {
	 * output.writePacket(buffer, 0, encodedBytes); } } } catch (EOFException e)
	 * { // This exception just provides exit from the loop } finally { try { if
	 * (input != null) { input.close(); } } finally { if (output != null) {
	 * output.close(); } } } }
	 */

	public byte[] loadBytesFromFile(File file) {
		WritableByteChannel outputChannel = null;
		FileChannel in = null;
		try {
			FileInputStream input = new FileInputStream(file);
			in = input.getChannel();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			outputChannel = Channels.newChannel(out);
			in.transferTo(0, in.size(), outputChannel);

			return out.toByteArray();

		} catch (IOException e) {
			Log.e(TAG, "IO exception reading encoded bytes", e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e1) {
				Log.e(TAG, "IO exception closing file", e1);
			}
			try {
				if (outputChannel != null)
					outputChannel.close();
			} catch (IOException e) {
				Log.e(TAG, "IO exception closing output file", e);
			}
		}
		return new byte[0];
	}

//	public InputStream getInputStream() throws IOException {
////		pipedOut = new PipedOutputStream();
////		pipedIn = new PipedInputStream(pipedOut);
////		return pipedIn;
//		Log.i(TAG, "initing Fifo");
//		mEncoder.initFifo(fifoPath);
//		return new FileInputStream(fifoPath);
//	}

	void WriteWavFile(File outputFile, int sampleRate, byte data[]) {
		long longSampleRate = sampleRate;
		int totalDataLen = data.length + 36;
		int totalAudioLen = data.length;
		long byteRate = longSampleRate * 2;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Exception opening file", e);
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
			Log.e(TAG, "IO exception saving wav file", e);
		}
	}

	public void stop() {
		mIsRecording = false;
	}
	
	public boolean getIsRecording() {
		return mIsRecording;
	}
}
