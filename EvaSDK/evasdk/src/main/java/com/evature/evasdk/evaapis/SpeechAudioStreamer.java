package com.evature.evasdk.evaapis;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.evature.evasdk.util.DLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

//import org.apache.commons.io.FileUtils;


/***
 * Reads data of the microphone, encodes it and adds the encoded data to a Queue
 * @author iftah
 *
 */
public class SpeechAudioStreamer {
	public static final String TAG = "SpeechAudioStreamer";

	// boolean mDebugSavePCM = true;
	
	// debug - time measurments
	public long totalTimeRecording;  // time from recording start to end (VAD detection)
	

	private AudioRecord mRecorder;
	private FLACStreamEncoder mEncoder;
	private byte[] mEncoderBuffer = null;
	private boolean mIsRecording = false;
    private boolean mIsFakeingAudio = false;

	// VAD Parameters,  default values
	private long mPreVadRecordingTime = 150; 	// time from start of recording where no VAD is considered
	private long mNoisePeriod = 200;   			// minimum continuous noisy time to be considered "Noise"
	private long mSilencePeriod = 500; 			// minimum continuous silent time to be considered "Silence"

    private final int AGGRESSIVNESS = 3; // 0..3 A more aggressive (higher mode) VAD is more restrictive in reporting speech
    private final int VAD_BUFFER_SAMPLES = 10*SAMPLE_RATE/1000; // X ms of buffer at 16 samples per 1ms.  WebRTC VAD requires Buffer to be one of 10ms, 20ms, 30ms

	
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;

	int mBufferIndex = 0;  // cyclic buffers index (cell used is:  index modulo buffer size)
	float mSoundLevelBuffer[] = new float[26]; // this is passed to the visualization of the sound-level - 26 amplitude points for wavy line (the horizontal density of the waves)
    final int  VISUALIZATION_TIMESTAMP = 30; // add volume point every (roughly) X ms (the speed the waves move)

    short[] mVADbuffer = new short[VAD_BUFFER_SAMPLES];
    int mVADbufferSize = 0;  // as the buffer gets filled this is increased

	long mStartLastSilence = -1;
	long mStartLastNoise = -1;
    long mLastVisualizationTime = 0;
    float mAccumulator = 0;
    int mAccumulatedSamples = 0;

    private float mPeakSoundLevel;
	private float mMinSoundLevel;
	
	public boolean wasNoise;
	private int mRecorderBufferSize;
    VADWebRTC  vadWebRTC;



	public SpeechAudioStreamer() {
//		fifoPath = context.getApplicationInfo().dataDir + "/flac_stream_fifo";

		totalTimeRecording = 0;
		
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//		mDebugSavePCM = prefs.getBoolean("eva_save_pcm", false);
//		mDebugSavePCM = false;
		
		
		mRecorderBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (mRecorderBufferSize <= 0) {
			DLog.e(TAG, "<<< bufferSize = " + mRecorderBufferSize);
			return;
		}
		mRecorderBufferSize *= 2; // for extra safety, do not loose audio even if delayed reading a bit
		mEncoderBuffer = new byte[mRecorderBufferSize];

        vadWebRTC = new VADWebRTC();
        vadWebRTC.init(AGGRESSIVNESS);
		DLog.i(TAG, "<<< Initialized buffer to "+mRecorderBufferSize);
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

		vadWebRTC.reset();
		return true;
	}
	
	
	public float[] getSoundLevelBuffer() {
		return mSoundLevelBuffer;
		//return mBufferShorts;
	}
	public int getBufferIndex() {
		//return Math.min(mBufferIndex, mEncoderBuffer.length) / 2;
		return mBufferIndex;
	}
	
	public int getMinSoundLevel() {
		return (int)mMinSoundLevel;
	}
	
	public void addVolumeSampleForVisulazation(float volumeSample) {

		mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ] =  volumeSample; // movingAverage;
		//Log.d(TAG, "@@@ added "+mSoundLevelBuffer[mBufferIndex % mSoundLevelBuffer.length ]+"  to buffer, index now at "+mBufferIndex);
		mBufferIndex++;
	}

	long sampleTimestamp = -1;

    /****
     *
     * @param audioBuffer
     * @param startOfRecording
     *
     * @return      False   if enough consecutive noise to start waiting for silence
     *              True    if enough consecutive silence to stop the recording
     *              null    otherwise
     */
	private Boolean checkForSilence(short[] audioBuffer, long startOfRecording) {

		// reduce Peak and increase minSound - to allow temporary peak/min to disappear over time
//		float dist = mPeakSoundLevel - mMinSoundLevel;
//		float factor = dist * 0.01f;
//		mPeakSoundLevel -= factor;
//		mMinSoundLevel += factor;

        long now = System.currentTimeMillis();
        sampleTimestamp += audioBuffer.length / 16; // 16ms per sample


        long timeRecording = now - startOfRecording;
        if (timeRecording < getPreVadRecordingTime()) {
            // too soon to consider VAD
            DLog.v(TAG, "Too soon to consider VAD");
            return null;
        }


        // identifying speech start by sudden volume up in the last sample relative to the previous MOVING_AVERAGE_BUFFER_SIZE samples
//		if (mBufferIndex > MOVING_AVERAGE_BUFFER_SIZE && currentVolume > (2.0/MOVING_AVERAGE_BUFFER_SIZE) * movingAverage) {
//			// this last sample was half of the twice its part of last accumulation buffer
//			// this was a noise sample
//			return false;
//		}

		int result = vadWebRTC.voiceActivity(audioBuffer, VAD_BUFFER_SAMPLES, SAMPLE_RATE);
        boolean silence = result == 0;
        boolean noise = result == 1;

		//float volumeLevelFraction = ((float)(movingAverage - mMinSoundLevel))  / (mPeakSoundLevel-mMinSoundLevel);
        //boolean silence = volumeLevelFraction <=  0.20f;
//		Log.i(TAG, "current D: "+(temp - mMinSoundLevel)+  "  peak D: "+(mPeakSoundLevel-mMinSoundLevel));
		if (silence) {
			mStartLastNoise = -1;
			
			// found start of silence ?
			if (mStartLastSilence == -1) {
				mStartLastSilence = now;
                //DLog.v(TAG, "VAD decided silence - - waiting.");
				return null;
			}
			
			// long time of silence ?
			if ((now - mStartLastSilence) > getSilencePeriod()) {
                //DLog.v(TAG, "VAD decided silence - - enough time!");
                return true;
			}
			// not enough silent time passed
            //DLog.v(TAG, "VAD decided silence - - waiting...");
			return null;
		} 

		// this is not a silence chunk
		mStartLastSilence = -1;

        // boolean noise = volumeLevelFraction >= 0.70f;
		if (noise) {
			// this is a noise sequence
			
			// start of noise?
			if (mStartLastNoise == -1) {
				mStartLastNoise = now;
                //DLog.v(TAG, "VAD decided SPEECH - - waiting.");
				return null;
			}
			
			// long time noise?
			if ((now - mStartLastNoise) > getNoisePeriod()) {
                //DLog.v(TAG, "VAD decided SPEECH - - enough time!");
				return false;
			}
			// not enough noisy time passed
            //DLog.v(TAG, "VAD decided SPEECH - - waiting...");
			return null;
		}

        //DLog.v(TAG, "VAD undecided?!");
		// this is not a noise chunk
		mStartLastNoise = -1;
		return null;
	}


	// called roughly every 50ms with 1600 bytes (depends on device)
	/***
     * Handle visulazation, check for silence
	 * @param numberOfReadBytes
	 * @param startOfRecording
	 */
	private void handleAudioBuffer(int numberOfReadBytes, long startOfRecording) {

		if (numberOfReadBytes == 0)
			return;
		
		//Log.d(TAG, "Read "+numberOfReadBytes);
		
		if (mEncoderBuffer.length != numberOfReadBytes) { // sanity
			DLog.w(TAG, "<<< unexpected numread="+numberOfReadBytes+" but buffer has "+ mEncoderBuffer.length);
			if (mEncoderBuffer.length < numberOfReadBytes) {
				numberOfReadBytes = mEncoderBuffer.length;
			}
		}


		// Analyze Sound.
		for (int i = 0; i < numberOfReadBytes; i += 2) {
			short sample = (short) ((mEncoderBuffer[i]) | mEncoderBuffer[i + 1] << 8);
            mVADbuffer[mVADbufferSize] = sample;
            mVADbufferSize++;
            mAccumulator += sample * sample;
            mAccumulatedSamples++;
            if (mAccumulatedSamples % (SAMPLE_RATE/1000) == 0) {
                sampleTimestamp++;  // every 16 samples is 1ms
            }

            // handle visulization if enough time passed since last visualization
            if ((sampleTimestamp - mLastVisualizationTime) >= VISUALIZATION_TIMESTAMP) {
                float currentVolume = mAccumulator / mAccumulatedSamples;
                mAccumulator = 0.0f;
                mAccumulatedSamples = 0;

                mLastVisualizationTime = sampleTimestamp;
                addVolumeSampleForVisulazation(currentVolume);

                if (currentVolume > mPeakSoundLevel) {
                    mPeakSoundLevel = currentVolume;
                }
                if (currentVolume < mMinSoundLevel) {
                    mMinSoundLevel = currentVolume;
                }

            }

            if (mVADbufferSize == mVADbuffer.length) {
                mVADbufferSize = 0;

                Boolean hadSilence = checkForSilence(mVADbuffer, startOfRecording);

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


	/****
	 * Read from Recorder and place in queue
	 */
	class Producer implements Runnable {
		private static final long MAX_TIME_WAIT_ITERATIONS = 300; // wait maximum 3 seconds
		private static final int MAX_ERROR_ITERATIONS = 5;
		//DataOutputStream dos = null;
		//FileOutputStream fos = null;
		FLACStreamEncoder encoder;
		final LinkedBlockingQueue<byte[]>  queue;

		Producer(FLACStreamEncoder encoder, final LinkedBlockingQueue<byte[]> queue) {
			this.encoder = encoder;
			this.queue = queue;
		}
		
		private void encode(byte[] chunk, int readSize) throws IOException {
			// long startTime = System.nanoTime();
			//Log.v(TAG, "Encode "+readSize+" bytes");
			ByteBuffer bf = ByteBuffer.allocateDirect(readSize);
			bf.put(chunk, 0, readSize);
			
			//ByteBuffer bf = ByteBuffer.wrap(chunk);
			
			encoder.write(bf, readSize);
			//encoder.flush();
		}

		public void run() {
            long timeIncludingFake = System.nanoTime();
			mAccumulator = 0.0f;
            mAccumulatedSamples = 0;
            mVADbufferSize = 0;

			DLog.d(TAG, "<<< Starting producer thread");

//			if (mDebugSavePCM) {
//				String fileBase = Environment.getExternalStorageDirectory()
//						.getPath() + "/sample";
//				File f = new File(fileBase + ".smp");
//
//				try {
//					fos = new FileOutputStream(f);
//					dos = new DataOutputStream(new BufferedOutputStream(fos));
//				} catch (FileNotFoundException e) {
//					DLog.w(TAG, "<<< Failed to open file to save PCM");
//				}
//			}
			

			try {
                try {
                    android.os.Process
                            .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    DLog.e(TAG, "<<< Set rec thread priority failed: " + e.getMessage());
                }


                for (int i = 0; i < mVADbuffer.length; i++) {
                    mVADbuffer[i] = 0;
                }
                for (int i = 0; i < mSoundLevelBuffer.length; i++) {
                    mSoundLevelBuffer[i] = 0;
                }
                for (int i = 0; i< mEncoderBuffer.length; i++) {
                    mEncoderBuffer[i] = 0;
                }

                int readSize = 10* SAMPLE_RATE* 2 / 1000 ; // 10ms, 16000 samples per second, 2 bytes per sample
                while (mIsFakeingAudio) {
                    // TODO: optimize - make same size and FLAC encoder inner buffers
                    encode(mEncoderBuffer, readSize);
                    Thread.sleep(10);
                }

                if (!mIsRecording) {
                    DLog.i(TAG, "<<< Finished producer thread - canceled before recording started");
                    return;
                }
                mRecorder.startRecording();

                int iterations = 0;
                int errorIterations = 0;

                long t0 = System.nanoTime() / 1000000;
                sampleTimestamp = t0;

                while (mIsRecording) {
                    readSize = mRecorder.read(mEncoderBuffer, 0, mEncoderBuffer.length);

                    iterations++;
                    if (readSize < 0) {
                        DLog.w(TAG, "<<< Error reading from recorder " + readSize);
                        Thread.sleep(10);
                        errorIterations++;
                        if (errorIterations > MAX_ERROR_ITERATIONS) {
                            DLog.e(TAG, "<<< Errors - quiting");
                            break;
                        }
                    } else if (readSize == 0) {
                        try {
                            DLog.i(TAG, "<<< Waiting for microphone to produce data");
                            if (iterations > MAX_TIME_WAIT_ITERATIONS) {
                                DLog.e(TAG, "<<< Waited too long for microphone to produce data - quiting");
                                break;
                            }
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            DLog.e(TAG, "<<< Interruprted Producer stream", e);
                        }
                    } else {

                        handleAudioBuffer(readSize, t0);

                        if (mIsRecording) {
                            try {
                                encode(mEncoderBuffer, readSize);
                            } catch (IOException e) {
                                DLog.e(TAG, "<<< IO error while sending to encoder");
                            }

                            //							if (mDebugSavePCM) {
                            //								dos.write(mEncoderBuffer, 0, readSize);
                            //							}
                            //							Thread.sleep(10);
                        }
                    }
                }
                encoder.flush();

                    /*if (mDebugSavePCM) {
                        dos.flush();
                        dos.close();

                        String fileBase = Environment.getExternalStorageDirectory().getPath() + "/sample";

                        byte bytes[] = FileUtils.readFileToByteArray(new File( fileBase + ".smp"));

                        WriteWavFile(new File(fileBase + ".wav"), 16000, bytes);
                    }*/

                //				queue.put(new byte[0]);

                long timeRecording = (System.nanoTime()/1000000) - t0;
                long timeRecordingWithFake = ((System.nanoTime() - timeIncludingFake) / 1000000);
                DLog.i(TAG, "<<< Finished producer thread - iterations=" + iterations + "  time real recording=" + timeRecording + "ms, time with fake="+timeRecordingWithFake);

			} catch (Exception ex) {
				DLog.e(TAG, "<<< Exception in microphone producer", ex);
			} finally {
				byte[] endOfRecording = new byte[0];
				queue.add(endOfRecording);
				mIsRecording = false;
                mIsFakeingAudio = false;

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
				totalTimeRecording = (System.nanoTime() - timeIncludingFake) / 1000000;
				DLog.d(TAG, "<<< All done.");
			}
		}

	}


    public void fakeAudioStreaming(final LinkedBlockingQueue<byte[]> queue) {
        if (mIsRecording) {
            DLog.w(TAG, "<<< Already recording - not starting 2nd time");
            return;
        }
        DLog.d(TAG, "<<< Starting streaming fake silence");
        mBufferIndex = 0;

        this.mEncoder = new FLACStreamEncoder();
        //mEncoder.init(fifoPath, mSampleRate, CHANNELS, 16, false, 256);

        mEncoder.setWriteCallback(new FLACStreamEncoder.WriteCallback() {

            public void onWrite(byte[] buffer, int length, int samples, int frame) {
                byte[] bufferCopy = new byte[length];
                System.arraycopy(buffer, 0, bufferCopy, 0, length);
                queue.add(bufferCopy);
            }
        });

        mIsRecording = true;
        mIsFakeingAudio = true;

        mEncoder.initWithCallback( SAMPLE_RATE, CHANNELS, 16, false, 256);
        DLog.i(TAG, "<<< Encoder initialized " + mEncoder.toString());
        Producer p = new Producer(mEncoder, queue);

        new Thread(p).start();

        DLog.i(TAG, "<<< Audio Fake Streamer started!");
    }


	public boolean startStreaming() {
		DLog.d(TAG, "<<< Starting streaming");
		mBufferIndex = 0;
		boolean success = initRecorder();
		if (!success) {
			DLog.e(TAG, "<<< Failed initializing recorder - not recording");
			return false;
		}

		mIsRecording = true;
        mIsFakeingAudio = false;

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
        mIsFakeingAudio = false;
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
