/*
 *  Copyright (c) 2014 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */


#include <memory>
#include <android/log.h>

#include "jni/jni_utils.h"

#include "webrtc/base/checks.h"

#include "webrtc/common_audio/vad/include/vad.h"
#include "jni/VADWebRTC.h"


/*****************************************************************************
 * Constants
 **/
static char const * const LTAG = "VADWebRTC/native";



VADWebRTC::VADWebRTC(VADWebRTC::Aggressiveness aggressiveness)
      : handle_(nullptr), aggressiveness_(aggressiveness) {
reset();
}

VADWebRTC::~VADWebRTC()  { WebRtcVad_Free(handle_); }


   // Calculates a VAD decision for the given audio frame. Valid sample rates
   // are 8000, 16000, and 32000 Hz; the number of samples must be such that the
   // frame is 10, 20, or 30 ms long.
  VADWebRTC::VoiceActivity VADWebRTC::voiceActivity(const int16_t* audio,
                         size_t num_samples,
                         int sample_rate_hz)  {
    int ret = WebRtcVad_Process(handle_, sample_rate_hz, audio, num_samples);
    switch (ret) {
      case 0:
        return kPassive;
      case 1:
        return kActive;
      default:
        RTC_NOTREACHED() << "WebRtcVad_Process returned an error.";
        return kError;
    }
  }

  void VADWebRTC::reset() {
    if (handle_)
      WebRtcVad_Free(handle_);
    handle_ = WebRtcVad_Create();
    RTC_CHECK(handle_);
    RTC_CHECK_EQ(WebRtcVad_Init(handle_), 0);
    RTC_CHECK_EQ(WebRtcVad_set_mode(handle_, aggressiveness_), 0);
  }






//--------------------------------
// JNI Wrappers
extern "C" {


/*****************************************************************************
 * Helper functions
 **/

/**
 * Retrieve VADWebRTC instance from the passed jobject.
 **/
VADWebRTC * get_vad(JNIEnv * env, jobject obj);

/**
 * Store VADWebRTC instance in the passed jobject.
 **/
void set_vad(JNIEnv * env, jobject obj,	VADWebRTC * vad);



JNIEXPORT
void Java_com_evature_evasdk_evaapis_VADWebRTC_init(JNIEnv * env, jobject obj, jint aggressiveness) {
	static_assert(sizeof(jlong) >= sizeof(VADWebRTC *), "jlong smaller than pointer");

	VADWebRTC * vad = new VADWebRTC(static_cast<VADWebRTC::Aggressiveness>(aggressiveness));

	__android_log_print(ANDROID_LOG_INFO, LTAG, "init VAD WebRTC aggressivness=%d", aggressiveness);

	set_vad(env, obj, vad);
}


JNIEXPORT
void Java_com_evature_evasdk_evaapis_VADWebRTC_deinit(JNIEnv * env, jobject obj) {
	VADWebRTC * vad = get_vad(env, obj);
	delete vad;
	set_vad(env, obj, NULL);
}

JNIEXPORT
void Java_com_evature_evasdk_evaapis_VADWebRTC_reset(JNIEnv * env, jobject obj) {
	VADWebRTC * vad = get_vad(env, obj);
	vad->reset();
}

JNIEXPORT jint Java_com_evature_evasdk_evaapis_VADWebRTC_voiceActivity(JNIEnv * env, jobject obj,
	jshortArray audio, jint num_samples, jint sample_rate_hz)
{
	VADWebRTC * vad = get_vad(env, obj);

	if (NULL == vad) {
		throwByName(env, IllegalArgumentException_classname,
				"Called without a valid vad instance!");
		return 0;
	}

	int16_t* buf = env->GetShortArrayElements(audio, NULL);

	jint result = vad->voiceActivity(buf, num_samples, sample_rate_hz);

    env->ReleaseShortArrayElements(audio, buf, 0);
	return result;
}

} // extern "C"

