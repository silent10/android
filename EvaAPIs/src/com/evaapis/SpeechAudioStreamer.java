package com.evaapis;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.OggSpeexWriter;
import org.xiph.speex.SpeexEncoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class SpeechAudioStreamer
{
	public static final String TAG = "SpeechAudioStreamer";
//	private PipedInputStream pipedIn;
//	private PipedOutputStream pipedOut;
	
	boolean mDebugRecording = true;

	private AudioRecord mRecorder;
	private byte[] mBuffer;
	private boolean mIsRecording = false;

	public static final int TEMP_BUFFER_SIZE = 5;
	private static final long SILENCE_PERIOD = 2000;
	private static final float SILENCE_THRESHOLD = 350;
	public static final int SAMPLE_RATE = 16000;
	public static final int CHANNELS = 1;
	public static final int SPEEX_MODE = 1;
	public static final int SPEEX_QUALITY = 8;

	int mSilenceAccumulationBufferIndex           = 0;
	float mSilenceAccumulationBuffer[] = new float[TEMP_BUFFER_SIZE];

	long mLastStart = -1;
	private int mSoundLevel;

	public boolean done = false;
	
	String fileBase;
	private Thread consumerThread;
	
	public SpeechAudioStreamer(int sampleRate) throws Exception {
		fileBase = Environment.getExternalStorageDirectory().getPath() + "/sampling";
	}


	void initRecorder() {
		Log.e("EVA","Starting to record");
		int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		mBuffer = new byte[bufferSize];
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		done = false;
		mIsRecording = true;
		mRecorder.startRecording();
	}

	


	private boolean checkForSilence(int numberOfReadBytes) {
		float totalAbsValue = 0.0f;
		short sample        = 0; 

		if(numberOfReadBytes==0) return false;
		// Analyze Sound.
		for( int i=0; i<mBuffer.length; i+=2 ) 
		{
			sample = (short)( (mBuffer[i]) | mBuffer[i + 1] << 8 );
			totalAbsValue += Math.abs( sample );
		}
		totalAbsValue /= (numberOfReadBytes/2);

		// Analyze temp buffer.
		mSilenceAccumulationBuffer[mSilenceAccumulationBufferIndex%TEMP_BUFFER_SIZE] = totalAbsValue;
		float temp                   = 0.0f;
		for( int i=0; i<TEMP_BUFFER_SIZE; ++i )
			temp += mSilenceAccumulationBuffer[i];

		mSilenceAccumulationBufferIndex++;

		this.mSoundLevel = (int)temp;
		
		if((temp>=0) && (temp <= SILENCE_THRESHOLD))
		{
			if(mLastStart==-1)
			{
				mLastStart = System.currentTimeMillis();
			}
			else
			{
				if((System.currentTimeMillis()-mLastStart) > SILENCE_PERIOD)
				{
					return true;
				}
			}
		}
		else
		{
			mLastStart = -1;
		}

		return false;
	}

	
	public int getSoundLevel() {
		return mSoundLevel;
	}


	class Producer implements Runnable 
	{
		private final BlockingQueue queue;

		Producer(BlockingQueue q) { 
			queue = q;
		}

		public void run() {
			try {

				for(int i=0;i<TEMP_BUFFER_SIZE;i++)
				{
					mSilenceAccumulationBuffer[i]=0.0f;
				}

				try {
					android.os.Process.setThreadPriority(
							android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					Log.e("EVA","Set rec thread priority failed: " + e.getMessage());
				}

				while (mIsRecording) {
					//int packetSize = 2 * CHANNELS * mEncoder.speexEncoder.getFrameSize();
					int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);



				//	Log.i("EVA","Read:"+readSize);

					if(readSize==0)
					{
						try {
							this.wait(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						if(true==checkForSilence(readSize))
						{
							Log.i(TAG, "Found silence"); 
							mIsRecording = false;  // Note this isn't the only way to stop recording - also possible from outside change of IsRecording to false
						}
						else
						{
							byte[] chunk = new byte[readSize];
							System.arraycopy(mBuffer, 0, chunk, 0, readSize);

							queue.put(chunk);

							Thread.sleep(10);
						}
					}

				}
				
				queue.put(new byte[0]);


				Log.i("EVA","Finished producer thread");


			} catch (InterruptedException ex)
			{
				Log.i(TAG, "Interrupted recorder thread");
			}
			finally {
				mRecorder.stop();
				mRecorder.release();				
			}
		}

	}

	public File getWavFile() {
		return new File(fileBase+".wav");
	}
	public File getSpeexFile() {
		return new File(fileBase+".smx");
	}
	public File getSmpFile() {
		return  new File(fileBase+".smp");
	}
	
	class Consumer implements Runnable {
		private final BlockingQueue queue;

		DataOutputStream dos=null;
		FileOutputStream fos = null;
		int mAccumulationBufferPosition;
		int mFrameSize;


		byte [] mAccumulationBuffer;
		byte [] mSecondaryBuffer;


		private final static int SINGLE_FRAME_SIZE = 32000;

		
		Consumer(BlockingQueue q) {
			mAccumulationBufferPosition = 0;
			mFrameSize=SINGLE_FRAME_SIZE;
			mAccumulationBuffer = new byte[2*mFrameSize];
			mSecondaryBuffer = new byte[2*mFrameSize];
			queue = q; 
		}
		public void run() {
			if(mDebugRecording)
			{
				File f = getSmpFile();

				try {
					fos = new FileOutputStream(f);
					dos = new DataOutputStream(new BufferedOutputStream(fos));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}    

			try {
				while (mIsRecording) {
					consume(queue.take()); 
				}

			} catch (InterruptedException ex) {
				Log.i(TAG, "Interrupted encoding thread");
			}

			try {
				Log.i(TAG,"Closing pipe");
//				pipedOut.flush();
//				pipedOut.close();

				if(mDebugRecording)
				{
					dos.flush();
					Log.i(TAG,"Flushed");
					dos.close();
					Log.i(TAG,"Closed");
//					encodeFile(getSmpFile(), getSpeexFile());
//					Log.i(TAG,"Encoded");

					byte bytes[] = FileUtils.readFileToByteArray(getSmpFile());
					Log.i(TAG,"Read "+bytes.length+" bytes");

					WriteWavFile(getWavFile(), 16000, bytes);
					Log.i(TAG,"Saved as Wav file");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			int count=0;
//			while(DictationHTTPClient.getInTransaction() && (count<MAX_WAIT_FOR_TRANSFER))
//			{
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				count++;
//			}
//			
//			if(DictationHTTPClient.getInTransaction())
//			{
//				DictationHTTPClient.stopTransfer();
//			}

			Log.i("EVA","Finished consumer thread");
			done = true;
		}
		
		void consume(Object x)
		{
			byte [] buffer = (byte[])x;
			byte[] encoded=null;
			byte[] chunk = new byte[mFrameSize];
			
			if(buffer.length==0)
			{
				Log.i(TAG,"Buffer length is 0 -- finishing!");
				System.arraycopy(mAccumulationBuffer, 0, chunk, 0, mAccumulationBufferPosition);
				try {
					encoded = chunk;//mEncoder.encode(chunk);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
//					pipedOut.write(encoded);
					if(mDebugRecording)
					{
						Log.i(TAG,"debug saving "+encoded.length);
						dos.write(encoded);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return;
			}
			

			System.arraycopy(buffer, 0, mAccumulationBuffer, mAccumulationBufferPosition, buffer.length);

			mAccumulationBufferPosition+=buffer.length;

			while(mAccumulationBufferPosition>=mFrameSize)
			{
				System.arraycopy(mAccumulationBuffer, 0, chunk, 0, mFrameSize);
				System.arraycopy(mAccumulationBuffer, mFrameSize, 
						mSecondaryBuffer, 0, mAccumulationBufferPosition- mFrameSize);
				byte []tmpBuffer = mAccumulationBuffer;
				mAccumulationBuffer= mSecondaryBuffer;
				mSecondaryBuffer = tmpBuffer;
				
				mAccumulationBufferPosition=mAccumulationBufferPosition-mFrameSize;
				
				try {
					encoded = chunk; //mEncoder.encode(chunk);
//					pipedOut.write(encoded);
					if(mDebugRecording)
					{
						Log.i(TAG,"debug saving "+encoded.length);
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

	void start()
	{
//		pipedOut = new PipedOutputStream();
//		try {
//			pipedIn = new PipedInputStream(pipedOut);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 

		BlockingQueue q = new ArrayBlockingQueue<byte []>(1000);
		Producer p = new Producer(q);
		Consumer c = new Consumer(q);
		mIsRecording = true;
		new Thread(p).start();
		consumerThread = new Thread(c);
		consumerThread.start();
	}

	void waitForIt() throws InterruptedException {
		consumerThread.join();
	}

	

	private void encodeFile(final File inputFile, final File outputFile) throws IOException {

		SpeexEncoder encoder = new SpeexEncoder();
		encoder.init(SPEEX_MODE, SPEEX_QUALITY, SAMPLE_RATE, CHANNELS);

		DataInputStream input = null;
		AudioFileWriter output = null;
		try {
			input = new DataInputStream(new FileInputStream(inputFile));
			output = new OggSpeexWriter(SPEEX_MODE, SAMPLE_RATE, CHANNELS, 1, false);
			output.open(outputFile);
			output.writeHeader("Encoded with: " + SpeexEncoder.VERSION);

			byte[] buffer = new byte[2560]; // 2560 is the maximum needed value (stereo UWB)
			int packetSize = 2 * CHANNELS * encoder.getFrameSize();

			while (true) {
				input.readFully(buffer, 0, packetSize);
				encoder.processData(buffer, 0, packetSize);
				int encodedBytes = encoder.getProcessedData(buffer, 0);
				if (encodedBytes > 0) {
					output.writePacket(buffer, 0, encodedBytes);
				}
			}
		} catch (EOFException e) {
			// This exception just provides exit from the loop
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} finally {
				if (output != null) {
					output.close();
				}
			}
		}
	}



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

//	public InputStream getInputStream() throws IOException {
//		return pipedIn;
//
//	}

	void WriteWavFile(File outputFile,int sampleRate,byte data[])
	{
		long longSampleRate = sampleRate;
		int totalDataLen = data.length+36;
		int totalAudioLen = data.length;
		long byteRate = longSampleRate*2;

		FileOutputStream out=null;
		try {
			out = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] header = new byte[44];
		header[0] = 'R';  // RIFF/WAVE header
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
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
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
		header[32] = (byte) (2 * 1);  // block align
		header[33] = 0;
		header[34] = 16;  // bits per sample
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
