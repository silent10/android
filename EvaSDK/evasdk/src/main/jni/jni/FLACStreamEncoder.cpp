

#include <alloca.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/stat.h>  /* Iftah:  for mkfifo */
#include <jni.h>
#include <android/log.h>


#include "FLAC/metadata.h"
#include "FLAC/stream_encoder.h"
#include "jni/jni_utils.h"
#include "jni/FLACStreamEncoder.h"


namespace {





/*****************************************************************************
 * Constants
 **/

static char const * const LTAG = "FLACStreamEncoder/native";

static int COMPRESSION_LEVEL = 5;





extern "C" {

	/***********
	 * encoder	The encoder instance calling the callback.
	 * buffer	An array of encoded data of length bytes.
	 * bytes	The byte length of buffer.
	 * samples	The number of samples encoded by buffer. 0 has a special meaning; see above.
	 * current_frame	The number of the current frame being encoded.
	 * client_data	The callee's client data set through FLAC__stream_encoder_init_*().
	 */
 	FLAC__StreamEncoderWriteStatus  encoder_WriteCallback(const FLAC__StreamEncoder *encoder,
		const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data);



	JavaVM * get_java_vm();
	jmethodID get_streamer_writeMethod();


}


/*****************************************************************************
 * Native FLACStreamEncoder representation
 *
 * FLACStreamEncoder uses a writer thread to write its internal buffer. The
 * implementation is deliberately simple, and writing functions like this:
 *
 * 1. There's a thread on which Java makes JNI calls to write some data, the
 *    JNI thread.
 *    There's also a thread on which data is written to disk via FLAC, the
 *    writer thread.
 * 2. Data is passed from the JNI thread to the writer thread via a locked
 *    singly linked list of buffers; the JNI thread appends buffers to the
 *    list, and once appended, relinquishes ownership which passes to the
 *    writer thread. The writer thread processes the list in a FIFO fashion;
 *    we'll call the list the write FIFO.
 * 3. Upon being called by Java to write data, the JNI thread writes the
 *    data to an internal buffer.
 *    If that buffer becomes full,
 *    a) it's appended to the write FIFO, and ownership is relinquished.
 *    b) a new buffer is allocated for subsequent write calls
 *    c) the writer thread is woken.
 **/


/**
 * Takes ownership of the outfile.
 * Iftah: pass NULL outfile in order to use the write_callback instead
 **/
FLACStreamEncoder::FLACStreamEncoder(char * outfile, int sample_rate, int channels,
		int bits_per_sample, bool verify, int frame_size, jobject obj) :
    m_outfile(outfile), m_sample_rate(sample_rate),
    m_channels(channels), m_bits_per_sample(bits_per_sample),
    m_encoder(NULL), m_max_amplitude(0), m_average_sum(0),
    m_average_count(0), m_write_buffer(NULL), m_write_buffer_size(0),
    m_write_buffer_offset(0), m_fifo(NULL), m_kill_writer(false)
    , m_verify(verify), m_frame_size(frame_size), m_obj(obj) {    }

/**
 * There are no exceptions here, so we need to "construct" outside the ctor.
 * Returns NULL on success, else an error message
 **/
char const * const FLACStreamEncoder::init() {

	// Try to create the encoder instance
	m_encoder = FLAC__stream_encoder_new();
	if (!m_encoder) {
		return "Could not create FLAC__StreamEncoder!";
	}

	// Try to initialize the encoder.
	FLAC__bool ok = true;
	ok &= FLAC__stream_encoder_set_sample_rate(m_encoder,
			1.0f * m_sample_rate);
	ok &= FLAC__stream_encoder_set_channels(m_encoder, m_channels);
	ok &= FLAC__stream_encoder_set_bits_per_sample(m_encoder,
			m_bits_per_sample);
	ok &= FLAC__stream_encoder_set_verify(m_encoder, m_verify);
	ok &= FLAC__stream_encoder_set_compression_level(m_encoder,
			COMPRESSION_LEVEL);
	ok &= FLAC__stream_encoder_set_blocksize(m_encoder, m_frame_size); // Iftah: attempt to write smaller chunks in higher frequency
	if (!ok) {
		return "Could not set up FLAC__StreamEncoder with the given parameters!";
	}


	// Try initializing the file stream.
	FLAC__StreamEncoderInitStatus init_status;

	__android_log_print(ANDROID_LOG_DEBUG, LTAG, "initializing encoder, m_obj=%p", m_obj);

	if (m_outfile != NULL) {
		init_status =
				FLAC__stream_encoder_init_file(m_encoder, m_outfile, NULL, NULL);
	}
	else {
		init_status = FLAC__stream_encoder_init_stream(
				m_encoder,
				encoder_WriteCallback,
				NULL,
				NULL,
				NULL,
				this

		);
	}

	if (FLAC__STREAM_ENCODER_INIT_STATUS_OK != init_status) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, "Could not initialize FLAC__StreamEncoder!");
	}
	else {
		__android_log_print(ANDROID_LOG_DEBUG, LTAG, "initialized encoder");
	}


	// Allocate write buffer. Based on observations noted down in issue #106, we'll
	// choose this to be 32k in size. Actual allocation happens lazily.
	//m_write_buffer_size = 32768;
	// Iftah: attempt to write smaller chunks in higher frequency
	m_write_buffer_size = m_frame_size*2;

	// The write FIFO gets created lazily. But we'll initialize the mutex for it
	// here.
	int err = pthread_mutex_init(&m_fifo_mutex, NULL);
	if (err) {
		return "Could not initialize FIFO mutex!";
	}

	// Similarly, create the condition variable for the writer thread.
	err = pthread_cond_init(&m_writer_condition, NULL);
	if (err) {
		return "Could not initialize writer thread condition!";
	}

	// Start thread!
	err = pthread_create(&m_writer, NULL,
			&FLACStreamEncoder::trampoline_func,
			new trampoline(this, &FLACStreamEncoder::writer_thread, NULL));
	if (err) {
		return "Could not start writer thread!";
	}



	return NULL;
}

/**
 * Destroys encoder instance, releases outfile
 **/
FLACStreamEncoder::~FLACStreamEncoder() {
	// Flush thread.
	flush_to_fifo();

	pthread_mutex_lock(&m_fifo_mutex);
	m_kill_writer = true;
	pthread_mutex_unlock(&m_fifo_mutex);

	pthread_cond_broadcast(&m_writer_condition);

	// Clean up thread related stuff.
	void * retval = NULL;
	pthread_join(m_writer, &retval);
	pthread_cond_destroy(&m_writer_condition);
	pthread_mutex_destroy(&m_fifo_mutex);

	// Clean up FLAC stuff
	if (m_encoder) {
		FLAC__stream_encoder_finish(m_encoder);
		FLAC__stream_encoder_delete(m_encoder);
		m_encoder = NULL;
	}

	if (m_outfile) {
		free(m_outfile);
		m_outfile = NULL;
	}

	if (m_obj) {
		JNIEnv* env;
		JavaVM *java_vm = get_java_vm();
		if (java_vm->GetEnv( (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
			__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>> GetEnv failed.");
		}
		else {
			env->DeleteGlobalRef(m_obj);
		}
	}
}

/**
 * Flushes internal buffers to disk.
 **/
void FLACStreamEncoder::flush() {
	//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "flush() called.");
	flush_to_fifo();

	// Signal writer to wake up.
	pthread_cond_signal(&m_writer_condition);
}

/**
 * Writes bufsize elements from buffer to the stream. Returns the number of
 * bytes actually written.
 **/
int FLACStreamEncoder::write(char * buffer, int bufsize) {
	//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Asked to write buffer of size %d", bufsize);

	// We have 8 or 16 bit pcm in the buffer, but FLAC expects 32 bit samples,
	// where some of the 32 bits are unused.
	int bufsize32 = bufsize / (m_bits_per_sample / 8);
	//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Required size: %d", bufsize32);

	// Protect from overly large buffers on the JNI side.
	if (bufsize32 > m_write_buffer_size) {
		// The only way we can handle this sanely without fragmenting buffers and
		// so forth is to use a separate code path here. In this, we'll flush the
		// current write buffer to the FIFO, and immediately append a new
		// FIFO entry that's as large as bufsize32.
		flush_to_fifo();

		m_write_buffer = new FLAC__int32[bufsize32];
		m_write_buffer_offset = 0;

		int ret = copyBuffer(buffer, bufsize, bufsize32);
		flush_to_fifo();

		// Signal writer to wake up.
		pthread_cond_signal(&m_writer_condition);
		return ret;
	}

	// If the current write buffer cannot hold the amount of data we've
	// got, push it onto the write FIFO and create a new buffer.
	if (m_write_buffer
			&& m_write_buffer_offset + bufsize32 > m_write_buffer_size) {
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG,
		//		"JNI buffer is full, pushing to FIFO - buffer contains %d", m_write_buffer_offset);
		flush_to_fifo();

		// Signal writer to wake up.
		pthread_cond_signal(&m_writer_condition);
	}

	// If we need to create a new buffer, do so now.
	if (!m_write_buffer) {
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Need new buffer.");
		m_write_buffer = new FLAC__int32[m_write_buffer_size];
		m_write_buffer_offset = 0;
	}

	// At this point we know that there's a write buffer, and we know that
	// there's enough space in it to write the data we've received.
	int ret = copyBuffer(buffer, bufsize, bufsize32);
	return ret;
}

/**
 * Writer thread function.
 **/
void * FLACStreamEncoder::writer_thread(void * args) {

//		__android_log_print(ANDROID_LOG_INFO, LTAG, "<<< Attaching thread");
	JNIEnv* env;
	JavaVM *java_vm = get_java_vm();
	java_vm->AttachCurrentThread(&env, NULL);

	// Loop while m_kill_writer is false.
	pthread_mutex_lock(&m_fifo_mutex);
	do {
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Going to sleep...");
		pthread_cond_wait(&m_writer_condition, &m_fifo_mutex);
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Wakeup: should I die after this? %s", (m_kill_writer ? "yes" : "no"));

		// Grab ownership over the current FIFO, and release the lock again.
		write_fifo_t * fifo = (write_fifo_t *) m_fifo;
		while (fifo) {
			m_fifo = NULL;
			pthread_mutex_unlock(&m_fifo_mutex);

			// Now we can take all the time we want to iterate over the FIFO's
			// contents. We just need to make sure to grab the lock again before
			// going into the next iteration of this loop.
			int retry = 0;

			write_fifo_t * current = fifo;
			while (current) {
//					__android_log_print(ANDROID_LOG_VERBOSE, LTAG,
//							"<<< Writer encoding size %d",
//							current->m_buffer_fill_size);
				//__android_log_print(ANDROID_LOG_VERBOSE, LTAG, "<<< Encoding current entry %p, buffer %p, size %d",
				//    current, current->m_buffer, current->m_buffer_fill_size);

				// Encode!
				FLAC__bool ok = FLAC__stream_encoder_process_interleaved(
						m_encoder, current->m_buffer,
						current->m_buffer_fill_size);
				if (ok) {
					retry = 0;
				} else {
					// We don't really know how much was written, we have to assume it was
					// nothing.
					if (++retry > 3) {
						__android_log_print(ANDROID_LOG_ERROR, LTAG,
								"Giving up on writing current FIFO!");
						break;
					} else {
						// Sleep a little before retrying.
						__android_log_print(ANDROID_LOG_ERROR, LTAG,
								"Writing FIFO entry %p failed; retrying...", current->m_buffer);
						usleep(5000); // 5msec
					}
					continue;
				}

				current = current->m_next;
			}

			// Once we've written everything, delete the fifo and grab the lock again.
			delete fifo;
			pthread_mutex_lock(&m_fifo_mutex);
			fifo = (write_fifo_t *) m_fifo;
		}

		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "End of wakeup, or should I die? %s", (m_kill_writer ? "yes" : "no"));
	} while (!m_kill_writer);

	pthread_mutex_unlock(&m_fifo_mutex);

//		__android_log_print(ANDROID_LOG_INFO, LTAG, "<<< Detaching thread");
	java_vm->DetachCurrentThread();

//		__android_log_print(ANDROID_LOG_VERBOSE, LTAG, "<<< Writer sleeping");

	//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Writer thread dies.");
	for (long i = 0; i < 50; i++) {
		usleep(5000);
	}
//		__android_log_print(ANDROID_LOG_VERBOSE, LTAG, "<<< Writer slept");
//    __android_log_print(ANDROID_LOG_DEBUG, LTAG, "slept.");
	return NULL;
}

float FLACStreamEncoder::getMaxAmplitude() {
	float result = m_max_amplitude;
	m_max_amplitude = 0;
	return result;
}

float FLACStreamEncoder::getAverageAmplitude() {
	float result = m_average_sum / m_average_count;
	m_average_sum = 0;
	m_average_count = 0;
	return result;
}




	// Thread trampoline
void * FLACStreamEncoder::trampoline_func(void * args) {
	trampoline * tramp = static_cast<trampoline *>(args);
	FLACStreamEncoder * encoder = tramp->m_encoder;
	trampoline::func_t func = tramp->m_func;

	void * result = (encoder->*func)(tramp->m_args);

	// Ownership tor tramp is passed to us, so we'll delete it here.
	delete tramp;
	return result;
}


} // anonymous namespace

/*****************************************************************************
 * JNI Wrappers
 **/


char * fifo_filename = 0;

extern "C" {

/*****************************************************************************
 * Helper functions
 **/

/**
 * Retrieve FLACStreamEncoder instance from the passed jobject.
 **/
FLACStreamEncoder * get_encoder(JNIEnv * env, jobject obj);

/**
 * Store FLACStreamEncoder instance in the passed jobject.
 **/
void set_encoder(JNIEnv * env, jobject obj, FLACStreamEncoder * encoder);


// Iftah:
/***********
 * encoder	The encoder instance calling the callback.
 * buffer	An array of encoded data of length bytes.
 * bytes	The byte length of buffer.
 * samples	The number of samples encoded by buffer. 0 has a special meaning; see above.
 * current_frame	The number of the current frame being encoded.
 * client_data	The callee's client data set through FLAC__stream_encoder_init_*().
 */
FLAC__StreamEncoderWriteStatus  encoder_WriteCallback(const FLAC__StreamEncoder *encoder,
		const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data) {

	FLACStreamEncoder* encoderCPP = (FLACStreamEncoder*)client_data;
	//__android_log_print(ANDROID_LOG_VERBOSE, LTAG, ">>>> Write Callback: %d bytes, %d samples, frame %d",  bytes, samples, current_frame);

	// Get JNI Env
	JNIEnv* env;
	JavaVM *java_vm = get_java_vm();
	if (java_vm->GetEnv( (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>> GetEnv failed.");
		return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
	}

	//__android_log_print(ANDROID_LOG_INFO, LTAG, ">>>> Write Callback: got the Java method, activating in %p", encoderCPP->m_obj);

	jbyteArray arr = env->NewByteArray(bytes);
	env->SetByteArrayRegion(arr, 0, bytes, (jbyte*) buffer);

    jmethodID streamer_writeMethod = get_streamer_writeMethod();
	env->CallVoidMethod(encoderCPP->m_obj, streamer_writeMethod, arr, bytes, samples, current_frame);
	if (env->ExceptionCheck()) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, ">>>> Write Callback: exception after calling method");
		env->ExceptionDescribe();
		return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
	}

	env->DeleteLocalRef(arr);

	return FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
}


JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_initFifo(JNIEnv * env,
		jobject obj, jstring outfile) {
	/*
	 Iftah:  I added the mkfifo part to allow streaming the data to Java...
	 */
	char * filename = convert_jstring_path(env, outfile);
	__android_log_print(ANDROID_LOG_INFO, LTAG, "unlinking %s", filename);
	int res = unlink(filename);
	__android_log_print(ANDROID_LOG_INFO, LTAG, "making fifo %s", filename);
	res = mkfifo(filename, 0777);
	fifo_filename = filename;
}

JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_deinitFifo(JNIEnv * env,
		jobject obj, jstring outfile) {
	unlink(convert_jstring_path(env, outfile));
}

JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_init(JNIEnv * env, jobject obj,
		jstring outfile, jint sample_rate, jint channels,
		jint bits_per_sample, jboolean verify, jint frame_size) {
	static_assert(sizeof(jlong) >= sizeof(FLACStreamEncoder *), "jlong smaller than pointer");

	FLACStreamEncoder * encoder = new FLACStreamEncoder(
			convert_jstring_path(env, outfile), sample_rate, channels,
			bits_per_sample, verify, frame_size, env->NewGlobalRef(obj));

	char const * const error = encoder->init();
	if (NULL != error) {
		delete encoder;

		throwByName(env, IllegalArgumentException_classname, error);
		return;
	}

	__android_log_print(ANDROID_LOG_INFO, LTAG, "init with outfile");
	set_encoder(env, obj, encoder);
}

/*
JNIEXPORT void JNICALL
Java_Callbacks_nativeMethod(JNIEnv *env, jobject obj, jint depth)
{
    printf("In C, depth = %d, about to enter Java\n", depth);
    (*env)->CallVoidMethod(env, obj, mid, depth);
    printf("In C, depth = %d, back from Java\n", depth);
}
*/

JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_initWithCallback(JNIEnv * env, jobject obj,
		jint sample_rate, jint channels,
		jint bits_per_sample, jboolean verify, jint frame_size) {
	static_assert(sizeof(jlong) >= sizeof(FLACStreamEncoder *), "jlong smaller than pointer");

	FLACStreamEncoder * encoder = new FLACStreamEncoder(
			NULL, sample_rate, channels,
			bits_per_sample, verify, frame_size, env->NewGlobalRef(obj));

	__android_log_print(ANDROID_LOG_INFO, LTAG, "init with write_callback");
	char const * const error = encoder->init();
	if (NULL != error) {
		__android_log_print(ANDROID_LOG_ERROR, LTAG, "error init %s", error);
		delete encoder;

		throwByName(env, IllegalArgumentException_classname, error);
		return;
	}

	set_encoder(env, obj, encoder);
}


JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_deinit(JNIEnv * env,
		jobject obj) {
	FLACStreamEncoder * encoder = get_encoder(env, obj);
	delete encoder;
	set_encoder(env, obj, NULL);
	if (fifo_filename) {
		__android_log_print(ANDROID_LOG_INFO, LTAG, "unlinking %s", fifo_filename);
		unlink(fifo_filename);
		fifo_filename = 0;
	}
}

JNIEXPORT jint Java_com_evature_evasdk_evaapis_FLACStreamEncoder_write(JNIEnv * env, jobject obj,
		jobject buffer, jint bufsize)
{
	FLACStreamEncoder * encoder = get_encoder(env, obj);

	if (NULL == encoder) {
		throwByName(env, IllegalArgumentException_classname,
				"Called without a valid encoder instance!");
		return 0;
	}

	if (bufsize > env->GetDirectBufferCapacity(buffer)) {
		throwByName(env, IllegalArgumentException_classname,
				"Asked to read more from a buffer than the buffer's capacity!");
	}

	char * buf = static_cast<char *>(env->GetDirectBufferAddress(buffer));
	return encoder->write(buf, bufsize);
}

JNIEXPORT
void Java_com_evature_evasdk_evaapis_FLACStreamEncoder_flush(JNIEnv * env,
		jobject obj) {
	FLACStreamEncoder * encoder = get_encoder(env, obj);

	if (NULL == encoder) {
		throwByName(env, IllegalArgumentException_classname,
				"Called without a valid encoder instance!");
		return;
	}

	encoder->flush();
}

JNIEXPORT jfloat Java_com_evature_evasdk_evaapis_FLACStreamEncoder_getMaxAmplitude(JNIEnv * env, jobject obj)
{
	FLACStreamEncoder * encoder = get_encoder(env, obj);

	if (NULL == encoder) {
		throwByName(env, IllegalArgumentException_classname,
				"Called without a valid encoder instance!");
		return 0;
	}

	return encoder->getMaxAmplitude();
}

JNIEXPORT jfloat Java_com_evature_evasdk_evaapis_FLACStreamEncoder_getAverageAmplitude(JNIEnv * env, jobject obj)
{
	FLACStreamEncoder * encoder = get_encoder(env, obj);

	if (NULL == encoder) {
		throwByName(env, IllegalArgumentException_classname,
				"Called without a valid encoder instance!");
		return 0;
	}

	return encoder->getAverageAmplitude();
}


} // extern "C"
