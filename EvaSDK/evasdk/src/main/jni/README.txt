FLAC and OGG sources were downloaded from http://www.xiph.org/downloads.

WebRTC sources were cloned from https://chromium.googlesource.com/external/webrtc/+/master/webrtc/

For license look in the respective license files provided in the sources.


WebRTC requires STL - I used `gnustl_static`  (see Application.mk) -  GNU STL license at https://gcc.gnu.org/onlinedocs/libstdc++/manual/license.html




-----

Note only part of the sources were included in the build - see Android.mk for the list

WebRTC - only sources relevant for VAD = Voice activity detection.   (not streaming, video, encoding, decoding, etc...)

