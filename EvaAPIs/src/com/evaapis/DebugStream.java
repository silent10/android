package com.evaapis;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class DebugStream extends InputStream {
	long timeOfLastBuffer = -1;
	
	private static final String TAG = "WrapStream";
	InputStream wrapped;
	
    public DebugStream(InputStream wrapped) {
    	Log.i(TAG, "<<< Started");
        this.wrapped = wrapped;
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
    	}
    	return result;
    }

    public int read(byte[] buffer) throws IOException {
        int result = wrapped.read(buffer);
        if (result == -1) {
        	timeOfLastBuffer = System.nanoTime();
        	Log.i(TAG, "<<< Input Stream ended");
        }
        return result;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
    	int result = wrapped.read(buffer, offset, length);
    	if (result == -1) {
    		timeOfLastBuffer = System.nanoTime();
    		Log.i(TAG, "<<< Input Stream ended");
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
