package com.evaapis;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class SpeechAudioStreamer {
	public static final String TAG = "SpeechAudioStreamer";
	private PipedInputStream pipedIn;
	private PipedOutputStream pipedOut;

	boolean mDebugRecording = true;

	private AudioRecord mRecorder;
	private FLACStreamEncoder mEncoder;
	private byte[] mBuffer;
	private boolean mIsRecording = false;

	public static final int TEMP_BUFFER_SIZE = 5;
	private static final long SILENCE_PERIOD = 700;
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;
	private int mSampleRate;

	int mSilenceAccumulationBufferIndex = 0;
	float mSilenceAccumulationBuffer[] = new float[TEMP_BUFFER_SIZE];
	int mSoundLevelBuffer[] = new int[250];

	long mLastStart = -1;
	private int mSoundLevel;
	private int mPeakSoundLevel;
	
	boolean wasNoise;

	private String fifoPath;
	private RandomAccessFile encodedFifo = null;

	public SpeechAudioStreamer(Context context, int sampleRate)
			throws Exception {
		fifoPath = context.getApplicationInfo().dataDir + "/flac_stream_fifo";

		Log.i(TAG, "initing fifo");
		this.mEncoder = new FLACStreamEncoder();
		mEncoder.initFifo(fifoPath);
		mSampleRate = sampleRate;

		Log.i(TAG, "Encoder=" + mEncoder.toString());
	}

	void initRecorder() {
		Log.e(TAG, "Starting to record");
		int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new byte[bufferSize];
		wasNoise = false;
		mPeakSoundLevel = 0;
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize);

	}
	
	
	public int[] getSoundLevelBuffer() {
		return mSoundLevelBuffer;
	}
	public int getBufferIndex() {
		return mSilenceAccumulationBufferIndex % mSoundLevelBuffer.length;
	}
	
	public int getPeakLevel() {
		return mPeakSoundLevel;
	}

	private Boolean checkForSilence(int numberOfReadBytes) {
		float totalAbsValue = 0.0f;
		short sample = 0;

		if (numberOfReadBytes == 0)
			return null;
		// Analyze Sound.
		for (int i = 0; i < mBuffer.length; i += 2) {
			sample = (short) ((mBuffer[i]) | mBuffer[i + 1] << 8);
			totalAbsValue += Math.abs(sample);
		}
		// average "sound level" of the current chunk
		totalAbsValue /= (numberOfReadBytes / 2);

		// Analyze temp buffer.
		mSilenceAccumulationBuffer[mSilenceAccumulationBufferIndex % TEMP_BUFFER_SIZE] = totalAbsValue;
		float temp = 0.0f;
		for (int i = 0; i < TEMP_BUFFER_SIZE; ++i)
			temp += mSilenceAccumulationBuffer[i];

		if (mSilenceAccumulationBufferIndex > TEMP_BUFFER_SIZE && totalAbsValue > (2.0/TEMP_BUFFER_SIZE) * temp) {
			// this last sample was half of the twice its part of last accumulation buffer
			// this was a noise sample
			return false;
		}

		this.mSoundLevel = (int) temp;
		mSoundLevelBuffer[mSilenceAccumulationBufferIndex % mSoundLevelBuffer.length ] = mSoundLevel;

		mSilenceAccumulationBufferIndex++;
		if (mSoundLevel > mPeakSoundLevel) {
			mPeakSoundLevel = mSoundLevel;
		}

		if (temp <= mPeakSoundLevel/2) { // became silent
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
			// noise
			mLastStart = -1;
		}

		return null;
	}

	public int getSoundLevel() {
		return mSoundLevel;
	}
	
	/****
	 * Read from Recorder and place in queue
	 */
	class Producer implements Runnable {
		private final BlockingQueue queue;

		Producer(BlockingQueue q) {
			queue = q;
		}

		public void run() {
			mRecorder.startRecording();

			try {

				for (int i = 0; i < TEMP_BUFFER_SIZE; i++) {
					mSilenceAccumulationBuffer[i] = 0.0f;
				}
				for (int i=0; i<mSoundLevelBuffer.length; i++) {
					mSoundLevelBuffer[i] = 0;
				}

				try {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					Log.e(TAG,
							"Set rec thread priority failed: " + e.getMessage());
				}

				while (mIsRecording) {
					// int packetSize = 2 * CHANNELS *
					// mEncoder.speexEncoder.getFrameSize();
					int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);

					// Log.i("EVA","Read:"+readSize);

					if (readSize == 0) {
						try {
							this.wait(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						Boolean hadSilence = checkForSilence(readSize);
						if (Boolean.FALSE == hadSilence) {
							wasNoise = true;
						}
						if (wasNoise && Boolean.TRUE == hadSilence) {
							mIsRecording = false;
						} else {
							byte[] chunk = new byte[readSize];
							System.arraycopy(mBuffer, 0, chunk, 0, readSize);
							Log.i(TAG, "Produce chunk " + chunk.length);
							queue.put(chunk);

							Thread.sleep(10);
						}
					}

				}

				queue.put(new byte[0]);

				Log.i(TAG, "Finished producer thread");

			} catch (InterruptedException ex) {
				Log.i(TAG, "Interrupted recorder thread");
			} finally {
				mRecorder.stop();
				mRecorder.release();
			}
		}

	}

	private final static int SINGLE_FRAME_SIZE = 32000;

	/***
	 * Read from Queue, buffer up and send to encoder
	 */
	class Consumer implements Runnable {
		private final BlockingQueue queue;

		DataOutputStream dos = null;
		FileOutputStream fos = null;
		int mAccumulationBufferPosition;
		int mFrameSize;

		byte[] mAccumulationBuffer;
		byte[] mSecondaryBuffer;

		Consumer(BlockingQueue q) {
			mAccumulationBufferPosition = 0;
			mFrameSize = SINGLE_FRAME_SIZE;
			mAccumulationBuffer = new byte[2 * mFrameSize];
			mSecondaryBuffer = new byte[2 * mFrameSize];
			queue = q;
		}

		private void encode(byte[] chunk) throws IOException {
			Log.i(TAG, "encoding " + chunk.length + " bytes");

			// encoded = mEncoder.encode(chunk);

			ByteBuffer bf = ByteBuffer.allocateDirect(chunk.length);
			bf.put(chunk);
			
			//ByteBuffer bf = ByteBuffer.wrap(chunk);
			
			mEncoder.write(bf, chunk.length);

			return;
		}

		public void run() {
			String fileBase = Environment.getExternalStorageDirectory()
					.getPath() + "/sample";

			if (mDebugRecording) {
				File f = new File(fileBase + ".smp");

				try {
					fos = new FileOutputStream(f);
					dos = new DataOutputStream(new BufferedOutputStream(fos));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			try {
				while (mIsRecording || !queue.isEmpty()) {
					consume(queue.take());
				}

			} catch (InterruptedException ex) {

			}

			try {
				mEncoder.flush();
				mEncoder.release();

				if (mDebugRecording) {
					dos.flush();
					dos.close();

					// encodeFile(new File(fileBase+".smp"), new
					// File(fileBase+".smx"));

					byte bytes[] = FileUtils.readFileToByteArray(new File(
							fileBase + ".smp"));

					WriteWavFile(new File(fileBase + ".wav"), 16000, bytes);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			// int count=0;
			// while(DictationHTTPClient.getInTransaction() &&
			// (count<MAX_WAIT_FOR_TRANSFER))
			// {
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// count++;
			// }
			//
			// if(DictationHTTPClient.getInTransaction())
			// {
			// DictationHTTPClient.stopTransfer();
			// }

			Log.i("EVA", "Finished consumer thread");

		}

		void consume(Object x) {
			byte[] buffer = (byte[]) x;
			byte[] encoded = null;
			byte[] chunk = new byte[mFrameSize];

			Log.i(TAG, "consuming chunk " + buffer.length);
			if (buffer.length == 0) {
				Log.i(TAG, "Buffer length is 0");
				System.arraycopy(mAccumulationBuffer, 0, chunk, 0,
						mAccumulationBufferPosition);
				if (chunk.length >0) {
					try {
						encode(chunk);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (mDebugRecording) {
						try {
							dos.write(chunk);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				

				return;
			}

			System.arraycopy(buffer, 0, mAccumulationBuffer,
					mAccumulationBufferPosition, buffer.length);

			mAccumulationBufferPosition += buffer.length;

			while (mAccumulationBufferPosition >= mFrameSize) {
				System.arraycopy(mAccumulationBuffer, 0, chunk, 0, mFrameSize);
				System.arraycopy(mAccumulationBuffer, mFrameSize,
						mSecondaryBuffer, 0, mAccumulationBufferPosition
								- mFrameSize);
				byte[] tmpBuffer = mAccumulationBuffer;
				mAccumulationBuffer = mSecondaryBuffer;
				mSecondaryBuffer = tmpBuffer;

				mAccumulationBufferPosition = mAccumulationBufferPosition
						- mFrameSize;

				try {
					// encoded = mEncoder.encode(chunk);
					encode(chunk);
					if (mDebugRecording) {
						dos.write(chunk);
					}
					Thread.sleep(20);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	/***
	 * Read from encoder Fifo and write to HTTP stream
	 */
	class Uploader implements Runnable {

		@Override
		public void run() {
			String fileBase = Environment.getExternalStorageDirectory()
					.getPath() + "/sample_encoded";

			DataOutputStream dos = null;
			
			if (mDebugRecording) {
				File f = new File(fileBase + ".fla");

				try {
					FileOutputStream fos = new FileOutputStream(f);
					dos = new DataOutputStream(new BufferedOutputStream(fos));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}


			
			Log.i(TAG, "initing Fifo");
			try {
				encodedFifo = new RandomAccessFile(fifoPath, "r");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			Log.i(TAG, "reading from Fifo");

			while(true) {
				
				byte[] encodeBuffer = new byte[SINGLE_FRAME_SIZE];
				int encodedLength = 0;
				try {
					encodedLength = encodedFifo.read(encodeBuffer);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Log.i(TAG, "got " + encodedLength + " encoded bytes");
				if (encodedLength < 0) {
					break;
				}
				byte[] encoded = new byte[encodedLength];
				System.arraycopy(encodeBuffer, 0, encoded, 0, encodedLength);

				try {
					pipedOut.write(encoded);
					
					if (mDebugRecording) {
						try {
							dos.write(encoded);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			Log.i(TAG, "Closing pipe");
			try {
				pipedOut.flush();
				pipedOut.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (mDebugRecording) {
				try {
				dos.flush();
				dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.i(TAG, "Done uploading");
		}

	}

	void start() {
		if (mIsRecording) {
			Log.w(TAG, "Already recording");
			return;
		}
		mIsRecording = true;
		BlockingQueue q = new ArrayBlockingQueue<byte[]>(1000);
		Producer p = new Producer(q);
		Consumer c = new Consumer(q);
		Uploader u = new Uploader();
		new Thread(u).start(); // start reading from FIFO - must read before any
								// write takes place
		try {
			Thread.sleep(20); // make sure reading has started
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.i(TAG, "encoder init");
		mEncoder.init(fifoPath, mSampleRate, CHANNELS, 16); // write header

		new Thread(p).start();
		new Thread(c).start();
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
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if (outputChannel != null)
					outputChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}

	public InputStream getInputStream() throws IOException {
		pipedOut = new PipedOutputStream();
		pipedIn = new PipedInputStream(pipedOut);
		return pipedIn;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		mIsRecording = false;
	}
}
