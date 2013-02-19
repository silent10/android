
#include <jni.h>
#include "include/speex/speex.h"

#ifdef __cplusplus
extern "C" {
#endif

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  JNIEnv *env;

  if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }

  return JNI_VERSION_1_6;
}

void *gSpeexEncoder;
SpeexBits bits;


jint JNICALL Java_com_evaapis_SpeexEncoder_getFrameSize(JNIEnv * env, jclass  obj)
{
	int ret;

	speex_encoder_ctl(gSpeexEncoder, SPEEX_GET_FRAME_SIZE, &ret);

	return ret;
}

void JNICALL Java_com_evaapis_SpeexEncoder_init(JNIEnv * env, jclass  obj,int iSpeexMode, int speexQuality, int sampleRate,int channels)
{
	gSpeexEncoder = speex_encoder_init(speex_lib_get_mode(iSpeexMode));
	speex_encoder_ctl(gSpeexEncoder, SPEEX_SET_QUALITY, &speexQuality);
	speex_encoder_ctl(gSpeexEncoder, SPEEX_SET_SAMPLING_RATE, &sampleRate);
	speex_bits_init(&bits);

}

void JNICALL Java_com_evaapis_SpeexEncoder_processData(JNIEnv * env, jclass  obj,jbyteArray temp, int i, int pcmPacketSize)
{
	jboolean copy;
	jbyte* buffer = (*env).GetByteArrayElements(temp, &copy);

	speex_bits_reset(&bits);
	speex_encode_int(gSpeexEncoder,(spx_int16_t*) buffer, &bits);

	(*env).ReleaseByteArrayElements( temp, buffer, 0);
}


JNIEXPORT jint JNICALL Java_com_evaapis_SpeexEncoder_getProcessedData(JNIEnv * env, jclass  obj,jbyteArray temp, int i)
{
	jboolean copy;

	jbyte* buffer = (*env).GetByteArrayElements(temp, &copy);
	int nbBytes = speex_bits_nbytes(&bits);
	speex_bits_write(&bits, (char *)buffer, nbBytes);
	(*env).ReleaseByteArrayElements( temp, buffer, 0);

	return nbBytes;
}
}
