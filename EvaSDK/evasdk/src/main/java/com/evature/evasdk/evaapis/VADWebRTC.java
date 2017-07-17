

package com.evature.evasdk.evaapis;

import java.nio.ByteBuffer;


/***
 * When moving this file - remember to fix FLACStreamEncoder.cpp
 * 1) Function names: eg. Java_com_evature_evaapis_VADWebRTC_init
 * 2) Class name const:  eg. VADWebRTC_classname
 * 3) And rebuild NDK:
 * ~/devel/android-ndk-???/ndk-build  from project folder above the `jni` folder
 * 4) move the resulting *.so files (in `libs` folder) to `jniLibs` folder
 */

public class VADWebRTC
{
  private static final String TAG = "VADWebRTC";

/***************************************************************************
   * Interface
   **/

  public VADWebRTC()
  {
      // touch the field and method to avoid Proguard removing them
      mObject = -1;
  }
  

  public void release()
  {
    deinit();
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
  public long  mObject;

// Sets the VAD operating mode. A more aggressive (higher mode) VAD is more
// restrictive in reporting speech. Put in other words the probability of being
// speech when the VAD returns 1 is increased with increasing mode. As a
// consequence also the missed detection rate goes up.
//    kVadNormal = 0,
//    kVadLowBitrate = 1,
//    kVadAggressive = 2,
//    kVadVeryAggressive = 3
  native public void init(int aggressiveness);

  native public void reset();

    // Calculates a VAD decision for the given audio frame. Valid sample rates
    // are 8000, 16000, and 32000 Hz; the number of samples must be such that the
    // frame is 10, 20, or 30 ms long.
  native public int voiceActivity(short[] buffer, int num_samples, int sample_rate_hz);

  /**
   * Destructor equivalent, but can be called multiple times.
   **/
  native private void deinit();

    // Load native library
    static {
        System.loadLibrary("audio-native");
    }

}
