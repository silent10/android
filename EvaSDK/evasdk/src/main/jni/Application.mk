# Build both machine code for all cpu architectures
APP_ABI := all

APP_MODULES = audio-flac audio-native
APP_OPTIM = release
APP_PLATFORM = android-19  # mkfifo isn't backwards compatible in 21+

APP_STL := gnustl_static

AUDIO_NATIVE_FLAGS = \
	-Ijni/config \
	-Ijni/ogg/include \
	-DVERSION=\"1.3\" \
	-Ijni/flac/include \
	-Ijni/flac/src/libFLAC/include \
	\
	-DWEBRTC_ANDROID=1 \
	-DWEBRTC_POSIX=1

APP_CFLAGS += $(AUDIO_NATIVE_FLAGS)
APP_CXXFLAGS += $(AUDIO_NATIVE_FLAGS)

APP_CXXFLAGS += \
	-std=c++11 \
	-isystem \
	-stdlib=libc++

