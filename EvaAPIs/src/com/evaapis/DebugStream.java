package com.evaapis;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class DebugStream extends InputStream {
	long timeOfLastBuffer = -1;
	
	private static final String TAG = "WrapStream";
	private boolean mDebugSave;
	private InputStream wrapped;
	private DataOutputStream dos;
	
    public DebugStream(InputStream wrapped, boolean save, String savePath) {
    	Log.i(TAG, "<<< Started");
        this.wrapped = wrapped;
        mDebugSave = save;

		if (mDebugSave) {
			File f = new File(savePath);

			try {
				FileOutputStream fos = new FileOutputStream(f);
				dos = new DataOutputStream(new BufferedOutputStream(fos));
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Failed to open debug file",e);
			}
		}
    }
    
    public int available() throws IOException {
        return wrapped.available();
    }
    public void close() throws IOException {
    	Log.i(TAG, "<<< Closed");
    	wrapped.close();
    }

    public void mark(int readlimit) {
    	wrapped.mark(readlimit);
    }

    public boolean markSupported() {
    	return wrapped.markSupported();
    }
    public int read() throws IOException {
    	Log.i(TAG, "<<< Read");
    	int result= wrapped.read();
    	if (result == -1) {
    		timeOfLastBuffer = System.nanoTime();
    		Log.i(TAG, "<<< Input Stream ended");
    		if (mDebugSave) {
				try {
				dos.flush();
				dos.close(); 
				} catch (IOException e) {
					Log.e(TAG, "Exception flusing debug file", e);
				}
			}
    	}
    	else if (mDebugSave) {
			try {
				dos.write(result);
			} catch (IOException e) {
				Log.w(TAG, "Exception writing debug file",e ); 
			}
		}
    	return result;
    }

    public int read(byte[] buffer) throws IOException {
        int result = wrapped.read(buffer);
        if (result == -1) {
        	timeOfLastBuffer = System.nanoTime();
        	Log.i(TAG, "<<< Input Stream ended");
        	if (mDebugSave) {
				try {
				dos.flush();
				dos.close(); 
				} catch (IOException e) {
					Log.e(TAG, "Exception flusing debug file", e);
				}
			}
        }
        else if (mDebugSave) {
			try {
				dos.write(buffer);
			} catch (IOException e) {
				Log.w(TAG, "Exception writing debug file",e ); 
			}
		}
        return result;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
    	int result = wrapped.read(buffer, offset, length);
    	if (result == -1) {
    		timeOfLastBuffer = System.nanoTime();
    		Log.i(TAG, "<<< Input Stream ended");
    	}
    	else if (mDebugSave) {
			try {
				dos.write(buffer, offset, length);
			} catch (IOException e) {
				Log.w(TAG, "Exception writing debug file",e ); 
			}
		}
    	return result;
    }

    public synchronized void reset() throws IOException {
    	Log.i(TAG, "reset");
        wrapped.reset();
    }

    public long skip(long byteCount) throws IOException {
        return wrapped.skip(byteCount);
    }
}
