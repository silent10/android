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
	flac/src/libFLAC/bitwriter.c

#	webrtc/modules/audio_processing/vad/gmm.cc \
#	webrtc/modules/audio_processing/vad/pitch_based_vad.cc \
#	webrtc/modules/audio_processing/vad/pitch_internal.cc \
#	webrtc/modules/audio_processing/vad/pole_zero_filter.cc \
#	webrtc/modules/audio_processing/vad/standalone_vad.cc \
#	webrtc/modules/audio_processing/vad/vad_audio_proc.cc \
#	webrtc/modules/audio_processing/vad/vad_circular_buffer.cc \
#	webrtc/modules/audio_processing/vad/voice_activity_detector.cc

include $(BUILD_STATIC_LIBRARY)

# Lastly build the JNI wrapper and link both other libs against it
#
include $(CLEAR_VARS)

LOCAL_MODULE    := audio-native
LOCAL_C_INCLUDES += $(LOCAL_PATH)/flac/include
LOCAL_SRC_FILES := \
	jni/FLACStreamEncoder.cpp
LOCAL_LDLIBS := -llog

LOCAL_STATIC_LIBRARIES := audio-flac

include $(BUILD_SHARED_LIBRARY)




