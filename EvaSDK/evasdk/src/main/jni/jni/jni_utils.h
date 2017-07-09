#ifndef JNI_UTILS_H_
#define JNI_UTILS_H_

// Define __STDINT_LIMITS to get INT8_MAX and INT16_MAX.
#define __STDINT_LIMITS 1
#include <stdint.h>
#include <limits.h>
#include <jni.h>

/*****************************************************************************
 * Very simple traits for int8_t/int16_t
 **/
template <typename intT>
struct type_traits
{
};


template <>
struct type_traits<int8_t>
{
  enum {
    MAX = INT8_MAX,
  };
};


template <>
struct type_traits<int16_t>
{
  enum {
    MAX = INT16_MAX,
  };
};



#ifdef __cplusplus
extern "C" {
#endif


char const * const IllegalArgumentException_classname = "java.lang.IllegalArgumentException";




/**
 * Convert a jstring to a UTF-8 char pointer. Ownership of the pointer goes
 * to the caller.
 **/
char * convert_jstring_path(JNIEnv * env, jstring input);


/**
 * Throws the given exception/message
 **/
void throwByName(JNIEnv * env, const char * name, const char * msg);


#ifdef __cplusplus
}
#endif


#endif