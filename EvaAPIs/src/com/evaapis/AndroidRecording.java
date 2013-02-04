package com.evaapis;

/*
 *  Copyright (c) 2012 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */


import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;


class AndroidRecording {
    private AudioRecord _audioRecord = null;


    private ByteBuffer _recBuffer;
    private byte[] _tempBufRec;

    private final ReentrantLock _recLock = new ReentrantLock();

    private boolean _doRecInit = true;

    private int _bufferedRecSamples = 0;
    private int _bufferedPlaySamples = 0;

    public AndroidRecording() {
        try {
    
            _recBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
                                                             // kHz
        } catch (Exception e) {
            DoLog(e.getMessage());
        }

        _tempBufRec = new byte[2 * 480];
    }

	public int InitRecording(int audioSource, int sampleRate) {
		// get the minimum buffer size that can be used
		int minRecBufSize =
				AudioRecord.getMinBufferSize(sampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);

		// DoLog("min rec buf size is " + minRecBufSize);

		// double size to be more safe
		int recBufSize = minRecBufSize * 2;
		_bufferedRecSamples = (5 * sampleRate) / 200;
		// DoLog("rough rec delay set to " + _bufferedRecSamples);

		// release the object
		if (_audioRecord != null) {
			_audioRecord.release();
			_audioRecord = null;
		}

		try {
			_audioRecord = new AudioRecord(
					audioSource,
					sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					recBufSize);

		} catch (Exception e) {
			DoLog(e.getMessage());
			return -1;
		}

		// check that the audioRecord is ready to be used
		if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
			// DoLog("rec not initialized " + sampleRate);
			return -1;
		}

		// DoLog("rec sample rate set to " + sampleRate);

		return _bufferedRecSamples;
	}

	public int StartRecording() {
		// start recording
		try {
			_audioRecord.startRecording();

		} catch (IllegalStateException e) {
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	public int StopRecording() {
		_recLock.lock();
		try {
			// only stop if we are recording
			if (_audioRecord.getRecordingState() ==
					AudioRecord.RECORDSTATE_RECORDING) {
				// stop recording
				try {
					_audioRecord.stop();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					return -1;
				}
			}

			// release the object
			_audioRecord.release();
			_audioRecord = null;

		} finally {
			// Ensure we always unlock, both for success, exception or error
			// return.
			_doRecInit = true;
			_recLock.unlock();
		}

		return 0;
	}

	public ByteBuffer getRecBuffer()
	{
		return _recBuffer;
	}
	
	
	public int RecordAudio(int lengthInBytes) {
		_recLock.lock();

		int readBytes = 0;
		
		try {
			if (_audioRecord == null) {
				return -2; // We have probably closed down while waiting for rec
				// lock
			}

			// Set priority, only do once
			if (_doRecInit == true) {
				try {
					android.os.Process.setThreadPriority(
							android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				} catch (Exception e) {
					DoLog("Set rec thread priority failed: " + e.getMessage());
				}
				_doRecInit = false;
			}

			
			_recBuffer.rewind(); // Reset the position to start of buffer
			readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);
			// DoLog("read " + readBytes + "from SC");
			_recBuffer.put(_tempBufRec);

		} catch (Exception e) {
			DoLogErr("RecordAudio try failed: " + e.getMessage());

		} finally {
			// Ensure we always unlock, both for success, exception or error
			// return.
			_recLock.unlock();
		}

		return (readBytes);
	}


	final String logTag = "AndroidRecording";

	private void DoLog(String msg) {
		Log.d(logTag, msg);
	}

	private void DoLogErr(String msg) {
		Log.e(logTag, msg);
	}
}

