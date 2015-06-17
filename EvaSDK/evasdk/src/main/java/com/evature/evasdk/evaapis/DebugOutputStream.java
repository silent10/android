package com.evature.evasdk.evaapis;

import com.evature.evasdk.util.DLog;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class DebugOutputStream extends OutputStream {
	long timeOfLastBuffer = -1;
	
	private static final String TAG = "WrapStream";
	private boolean mDebugSave;
	private OutputStream wrapped;
	private DataOutputStream dos;
	
    public DebugOutputStream(OutputStream wrapped, boolean save, String savePath) {
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
    
    public void close() throws IOException {
    	DLog.i(TAG, "<<< Closed");
    	wrapped.close();
    }


	@Override
	public void write(int oneByte) throws IOException {
		DLog.i(TAG, "<<< write oneByte");
    	wrapped.write(oneByte);
		if (mDebugSave) {
			try {
				dos.write(oneByte);
			} catch (IOException e) {
				DLog.w(TAG, "Exception writing debug file",e ); 
			}
		}
	}
	
	 public void flush() throws IOException {
	 	DLog.i(TAG, "<<< flush");
	 	wrapped.flush();
	 }

    public void write(byte[] buffer) throws IOException {
    	DLog.i(TAG, "<<< write buffer");
    	wrapped.write(buffer);
    	if (mDebugSave) {
			try {
				dos.write(buffer);
			} catch (IOException e) {
				DLog.w(TAG, "Exception writing debug file",e ); 
			}
		}
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
    	DLog.i(TAG, "<<< write buffer ["+offset+":+"+count+"]");
    	wrapped.write(buffer, offset, count);
    	if (mDebugSave) {
			try {
				dos.write(buffer, offset, count);
			} catch (IOException e) {
				DLog.w(TAG, "Exception writing debug file",e ); 
			}
		}
    }
}
