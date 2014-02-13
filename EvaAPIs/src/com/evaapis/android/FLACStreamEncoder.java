

package com.evaapis.android;

import java.nio.ByteBuffer;

/***
 * When moving this file - remember to fix FLACStreamEncoder.cpp
 * 1) Function names: eg. Java_com_evaapis_android_FLACStreamEncoder_init
 * 2) Class name const:  eg. FLACStreamEncoder_classname 
 * And rebuild NDK:
 * ~/devel/android-ndk-r8e/ndk-build  from EvaAPIs
 */

public class FLACStreamEncoder
{
  /***************************************************************************
   * Interface
   **/

  public FLACStreamEncoder()
  {
  }
  

  public void release()
  {
    deinit();
  }



  public void reset(String outfile, int sample_rate, int channels,
      int bits_per_sample)
  {
    deinit();
    init(outfile, sample_rate, channels, bits_per_sample);
  }



  protected void finalize() throws Throwable
  {
    try {
      deinit();
    } finally {
      super.finalize();
    }
  }



  /***************************************************************************
   * JNI Implementation
   **/

  // Pointer to opaque data in C
  private long  mObject;

  native public void initFifo(String outfile);
  
  /**
   * channels must be either 1 (mono) or 2 (stereo)
   * bits_per_sample must be either 8 or 16
   **/
  native public void init(String outfile, int sample_rate, int channels,
      int bits_per_sample);

  /**
   * Destructor equivalent, but can be called multiple times.
   **/
  native private void deinit();

  /**
   * Returns the maximum amplitude written to the file since the last call
   * to this function.
   **/
  native public float getMaxAmplitude();

  /**
   * Returns the average amplitude written to the file since the last call
   * to this function.
   **/
  native public float getAverageAmplitude();

  /**
   * Writes data to the encoder. The provided buffer must be at least as long
   * as the provided buffer size.
   * Returns the number of bytes actually written.
   **/
  native public int write(ByteBuffer buffer, int bufsize);

  /**
   * Flushes internal buffers to FIFO.
   **/
  native public void flush();

  // Load native library
  static {
    System.loadLibrary("audio-native");
  }
}
