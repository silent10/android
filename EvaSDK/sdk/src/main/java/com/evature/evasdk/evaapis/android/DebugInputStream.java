package com.evature.evasdk.evaapis.android;

import com.evature.evasdk.util.DLog;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


class DebugInputStream extends InputStream {
	long timeOfLastBuffer = -1;
	
	private static final String TAG = "WrapStream";
	private boolean mDebugSave;
	private InputStream wrapped;
	private DataOutputStream dos;
	
    public DebugInputStream(InputStream wrapped, boolean save, String savePath) {
    	DLog.i(TAG, "<<< Started");
        this.wrapped = wrapped;
        mDebugSave = save;

		if (mDebugSave) {
			File f = new File(savePath);

			try {
				FileOutputStream fos = new FileOutputStream(f);
				dos = new DataOutputStream(new BufferedOutputStream(fos));
			} catch (FileNotFoundException e) {
				DLog.e(TAG, "Failed to open debug file",e);
			}
		}
    }
    
    public int available() throws IOException {
        return wrapped.available();
    }
    public void close() throws IOException {
    	DLog.i(TAG, "<<< Closed");
    	wrapped.close();
    }

    public void mark(int readlimit) {
    	wrapped.mark(readlimit);
    }

    public boolean markSupported() {
    	return wrapped.markSupported();
    }
    public int read() throws IOException {
    	DLog.i(TAG, "<<< Read");
    	int result= wrapped.read();
    	if (result == -1) {
    		timeOfLastBuffer = System.nanoTime();
    		DLog.i(TAG, "<<< Input Stream ended");
    		if (mDebugSave) {
				try {
				dos.flush();
				dos.close(); 
				} catch (IOException e) {
					DLog.e(TAG, "Exception flusing debug file", e);
				}
			}
    	}
    	else {
    		if (mDebugSave) {
				try {
					dos.write(result);
				} catch (IOException e) {
					DLog.w(TAG, "Exception writing debug file",e ); 
				}
			}
    	}
    	DLog.i(TAG, "<<< Read "+result+" bytes");
    	return result;
    }

    public int read(byte[] buffer) throws IOException {
        int result = wrapped.read(buffer);
        if (result == -1) {
        	timeOfLastBuffer = System.nanoTime();
        	DLog.i(TAG, "<<< Input Stream ended");
        	if (mDebugSave) {
				try {
				dos.flush();
				dos.close(); 
				} catch (IOException e) {
					DLog.e(TAG, "Exception flusing debug file", e);
				}
			}
        }
        else {
			if (mDebugSave) {
				try {
					dos.write(buffer);
				} catch (IOException e) {
					DLog.w(TAG, "Exception writing debug file", e);
				}
			}
		}
        DLog.i(TAG, "<<< Read "+result+" bytes");
        return result;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
    	int result = wrapped.read(buffer, offset, length);
    	if (result == -1) {
    		timeOfLastBuffer = System.nanoTime();
    		DLog.i(TAG, "<<< Input Stream ended");
    	}
    	else {
			if (mDebugSave) {
				try {
					dos.write(buffer, offset, length);
				} catch (IOException e) {
					DLog.w(TAG, "Exception writing debug file",e ); 
				}
			}
    	}
    	DLog.i(TAG, "<<< Read "+result+" bytes");
    	return result;
    }

    public synchronized void reset() throws IOException {
    	DLog.i(TAG, "reset");
        wrapped.reset();
    }

    public long skip(long byteCount) throws IOException {
        return wrapped.skip(byteCount);
    }
}
