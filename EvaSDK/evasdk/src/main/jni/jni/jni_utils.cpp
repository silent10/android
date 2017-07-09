#include <jni.h>
#include <android/log.h>
#include <string.h>

#include "jni/jni_utils.h"

extern "C" {




/**
 * Convert a jstring to a UTF-8 char pointer. Ownership of the pointer goes
 * to the caller.
 **/
char * convert_jstring_path(JNIEnv * env, jstring input)
{
  char buf[PATH_MAX];

  jboolean copy = false;
  char const * str = env->GetStringUTFChars(input, &copy);
  if (NULL == str) {
    // OutOfMemoryError has already been thrown here.
    return NULL;
  }

  char * ret = strdup(str);
  env->ReleaseStringUTFChars(input, str);
  return ret;
}


/**
 * Throws the given exception/message
 **/
void throwByName(JNIEnv * env, const char * name, const char * msg)
{
  jclass cls = env->FindClass(name);

  // If cls is NULL, an exception has already been thrown
  if (NULL != cls) {
    env->ThrowNew(cls, msg);
    // Ignore return value of ThrowNew... all we could reasonably do is try and
    // throw another exception, after all.
  }

  env->DeleteLocalRef(cls);
}




} // extern "C"