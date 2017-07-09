LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := audio-flac

LOCAL_C_INCLUDES += $(LOCAL_PATH)/ogg/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/flac/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/flac/src/libFLAC/include

LOCAL_SRC_FILES := \
	ogg/src/bitwise.c \
	ogg/src/framing.c \
	flac/src/libFLAC/bitmath.c \
	flac/src/libFLAC/bitreader.c \
	flac/src/libFLAC/cpu.c \
	flac/src/libFLAC/crc.c \
	flac/src/libFLAC/fixed.c \
	flac/src/libFLAC/float.c \
	flac/src/libFLAC/format.c \
	flac/src/libFLAC/lpc.c \
	flac/src/libFLAC/md5.c \
	flac/src/libFLAC/memory.c \
	flac/src/libFLAC/metadata_iterators.c \
	flac/src/libFLAC/metadata_object.c \
	flac/src/libFLAC/ogg_decoder_aspect.c \
	flac/src/libFLAC/ogg_encoder_aspect.c \
	flac/src/libFLAC/ogg_helper.c \
	flac/src/libFLAC/ogg_mapping.c \
	flac/src/libFLAC/stream_decoder.c \
	flac/src/libFLAC/stream_encoder.c \
	flac/src/libFLAC/stream_encoder_framing.c \
	flac/src/libFLAC/window.c \
	flac/src/libFLAC/bitwriter.c \
	webrtc/base/checks.cc \
	webrtc/base/logging.cc \
	webrtc/base/criticalsection.cc \
	webrtc/base/event.cc \
	webrtc/base/thread_checker_impl.cc \
	webrtc/base/stringencode.cc \
	webrtc/base/stringutils.cc \
	webrtc/base/timeutils.cc \
	webrtc/modules/audio_processing/vad/gmm.cc \
	webrtc/modules/audio_processing/vad/pitch_based_vad.cc \
	webrtc/modules/audio_processing/vad/pitch_internal.cc \
	webrtc/modules/audio_processing/vad/pole_zero_filter.cc \
	webrtc/modules/audio_processing/vad/standalone_vad.cc \
	webrtc/modules/audio_processing/vad/vad_audio_proc.cc \
	webrtc/modules/audio_processing/vad/vad_circular_buffer.cc \
	webrtc/modules/audio_processing/vad/voice_activity_detector.cc  \
	webrtc/audio/utility/audio_frame_operations.cc \
	webrtc/common_audio/vad/vad.cc \
	webrtc/common_audio/vad/vad_core.c \
	webrtc/common_audio/vad/vad_filterbank.c \
	webrtc/common_audio/vad/vad_gmm.c \
	webrtc/common_audio/vad/vad_sp.c \
	webrtc/common_audio/vad/webrtc_vad.c \
	webrtc/common_audio/fft4g.c \
	webrtc/common_audio/signal_processing/dot_product_with_scale.cc \
	webrtc/common_audio/signal_processing/spl_init.c \
	webrtc/common_audio/signal_processing/division_operations.c \
	webrtc/common_audio/signal_processing/energy.c \
	webrtc/common_audio/signal_processing/resample_48khz.c \
	webrtc/common_audio/signal_processing/resample_by_2_internal.c \
	webrtc/common_audio/signal_processing/min_max_operations.c \
	webrtc/common_audio/signal_processing/cross_correlation.c \
	webrtc/common_audio/signal_processing/downsample_fast.c \
	webrtc/common_audio/signal_processing/vector_scaling_operations.c \
	webrtc/common_audio/signal_processing/get_scaling_square.c \
	webrtc/common_audio/signal_processing/resample_fractional.c

include $(BUILD_STATIC_LIBRARY)

# Lastly build the JNI wrapper and link both other libs against it
#
include $(CLEAR_VARS)

LOCAL_MODULE    := audio-native
LOCAL_C_INCLUDES += $(LOCAL_PATH)/flac/include
LOCAL_SRC_FILES := \
	jni/jni_utils.cpp \
	jni/FLACStreamEncoder.cpp \
	jni/VADWebRTC.cpp \
	jni/jni_onload.cpp
LOCAL_LDLIBS := -llog

LOCAL_STATIC_LIBRARIES := audio-flac

include $(BUILD_SHARED_LIBRARY)




