#include <assert.h>
#include <jni.h>
#include <android/log.h>

#include "FLACStreamEncoder.h"
#include "VADWebRTC.h"

extern "C" {


static char const * const FLACStreamEncoder_classname =	"com/evature/evasdk/evaapis/FLACStreamEncoder";
static char const * const FLACStreamEncoder_mObject = "mObject";
static char const * const FLACStreamEncoder_writeCallback = "writeCallback";

static char const * const VADWebRTC_classname =	"com/evature/evasdk/evaapis/VADWebRTC";
static char const * const VADWebRTC_mObject = "mObject";


static char const * const LTAG = "jni_onload/native";


static JavaVM *java_vm;

static jfieldID streamer_mObj;
static jmethodID streamer_writeMethod;

static jfieldID vad_mObj;



JavaVM * get_java_vm() {
	return java_vm;
}

jmethodID get_streamer_writeMethod() {
	return  streamer_writeMethod;
}

/*****************************************************************************
 * Helper functions
 **/

/**
 * Retrieve FLACStreamEncoder instance from the passed jobject.
 **/
FLACStreamEncoder * get_encoder(JNIEnv * env, jobject obj) {

	jlong encoder_value = env->GetLongField(obj, streamer_mObj);

	return reinterpret_cast<FLACStreamEncoder *>(encoder_value);
}

/**
 * Store FLACStreamEncoder instance in the passed jobject.
 **/
void set_encoder(JNIEnv * env, jobject obj,	FLACStreamEncoder * encoder) {

	jlong encoder_value = reinterpret_cast<jlong>(encoder);

	env->SetLongField(obj, streamer_mObj, encoder_value);
}


/**
 * Retrieve VADWebRTC instance from the passed jobject.
 **/
VADWebRTC * get_vad(JNIEnv * env, jobject obj) {

	jlong vad_value = env->GetLongField(obj, vad_mObj);

	return reinterpret_cast<VADWebRTC *>(vad_value);
}

/**
 * Store VADWebRTC instance in the passed jobject.
 **/
void set_vad(JNIEnv * env, jobject obj,	VADWebRTC * vad) {

	jlong vad_value = reinterpret_cast<jlong>(vad);

	env->SetLongField(obj, vad_mObj, vad_value);
}


jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
//	__android_log_print(ANDROID_LOG_INFO, LTAG, ">>> JNI_OnLoad");
    java_vm = vm;


	// Do the JNI dance for getting the mObject field, and write callback method
    JNIEnv* env;
	if (java_vm->GetEnv( (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>> GetEnv failed.");
		return JNI_ERR;
	}

	jclass streamer_jcls = env->FindClass(FLACStreamEncoder_classname);
	if (env->ExceptionCheck()) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>>  failed to get %s class", FLACStreamEncoder_classname);
		env->ExceptionDescribe();
	    return JNI_ERR;
	}

	static_assert(sizeof(jlong) >= sizeof(FLACStreamEncoder *), "jlong smaller than pointer");
	jfieldID object_field = env->GetFieldID(streamer_jcls, FLACStreamEncoder_mObject, "J");
	streamer_mObj = object_field;

    jmethodID mid = env->GetMethodID(streamer_jcls, FLACStreamEncoder_writeCallback, "([BIII)V");  // buffer, length, samples, frame
    if (env->ExceptionCheck()) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>>> Write Callback: failed to get %s method", FLACStreamEncoder_writeCallback);
		env->ExceptionDescribe();
		return JNI_ERR;
	}
	assert(mid != 0);
	streamer_writeMethod = mid;

	jclass vad_jcls = env->FindClass(VADWebRTC_classname);
	if (env->ExceptionCheck()) {
        __android_log_print(ANDROID_LOG_ERROR, LTAG, ">>>  failed to get %s class", VADWebRTC_classname);
        env->ExceptionDescribe();
        return JNI_ERR;
    }

    static_assert(sizeof(jlong) >= sizeof(VADWebRTC *), "jlong smaller than pointer");
    object_field = env->GetFieldID(vad_jcls, VADWebRTC_mObject, "J");
    vad_mObj = object_field;


    return JNI_VERSION_1_6;
}


} // extern "C"