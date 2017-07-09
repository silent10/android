#ifndef FLAC_STREAM_ENCODER_H_
#define FLAC_STREAM_ENCODER_H_

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

class FLACStreamEncoder {
public:
	jobject m_obj; // pointer to the java object

	// Write FIFO
	struct write_fifo_t {
		write_fifo_t(FLAC__int32 * buf, int fillsize) :
                m_next(NULL), m_buffer(buf) // Taking ownership here.
                        , m_buffer_fill_size(fillsize) {
        }

        ~write_fifo_t() {
            // We have ownership!
            delete[] m_buffer;
            delete m_next;
        }

        write_fifo_t * last() volatile {
            volatile write_fifo_t * last = this;
            while (last->m_next) {
                last = last->m_next;
            }
            return (write_fifo_t *) last;
        }

        write_fifo_t * m_next;
        FLAC__int32 * m_buffer;
        int m_buffer_fill_size;
    };

    // Thread trampoline arguments
    	struct trampoline {
    		typedef void * (FLACStreamEncoder::*func_t)(void * args);

    		FLACStreamEncoder * m_encoder;
    		func_t m_func;
    		void * m_args;

    		trampoline(FLACStreamEncoder * encoder, func_t func, void * args) :
    				m_encoder(encoder), m_func(func), m_args(args) {
    		}
    	};


	/**
	 * Takes ownership of the outfile.
	 * Iftah: pass NULL outfile in order to use the write_callback instead
	 **/
	FLACStreamEncoder(char * outfile, int sample_rate, int channels,
			int bits_per_sample, bool verify, int frame_size, jobject obj);

	/**
	 * There are no exceptions here, so we need to "construct" outside the ctor.
	 * Returns NULL on success, else an error message
	 **/
	char const * const init();

	/**
	 * Destroys encoder instance, releases outfile
	 **/
	~FLACStreamEncoder();

	/**
	 * Flushes internal buffers to disk.
	 **/
	void flush();

	/**
	 * Writes bufsize elements from buffer to the stream. Returns the number of
	 * bytes actually written.
	 **/
	int write(char * buffer, int bufsize);

	/**
	 * Writer thread function.
	 **/
	void * writer_thread(void * args);

	float getMaxAmplitude();

	float getAverageAmplitude();


private:
	/**
	 * Append current write buffer to FIFO, and clear it.
	 **/
	inline void flush_to_fifo() {
		if (!m_write_buffer) {
			return;
		}

		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Flushing to FIFO.");

		write_fifo_t * next = new write_fifo_t(m_write_buffer,
				m_write_buffer_offset);
		m_write_buffer = NULL;

		pthread_mutex_lock(&m_fifo_mutex);
		if (m_fifo) {
			write_fifo_t * last = m_fifo->last();
			last->m_next = next;
		} else {
			m_fifo = next;
		}
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "FIFO: %p, new entry: %p", m_fifo, next);
		pthread_mutex_unlock(&m_fifo_mutex);
	}

	/**
	 * Wrapper around templatized copyBuffer that writes to the current write
	 * buffer at the current offset.
	 **/
	inline int copyBuffer(char * buffer, int bufsize, int bufsize32) {
		FLAC__int32 * buf = m_write_buffer + m_write_buffer_offset;

		//__android_log_print(ANDROID_LOG_VERBOSE, LTAG, "Adding %d to JNI buffer", bufsize32);
		//__android_log_print(ANDROID_LOG_DEBUG, LTAG, "Writing at %p[%d] = %p", m_write_buffer, m_write_buffer_offset, buf);
		if (8 == m_bits_per_sample) {
			copyBuffer<int8_t>(buf, buffer, bufsize);
			m_write_buffer_offset += bufsize32;
		} else if (16 == m_bits_per_sample) {
			copyBuffer<int16_t>(buf, buffer, bufsize);
			m_write_buffer_offset += bufsize32;
		} else {
			// XXX should never happen, just exit.
			return 0;
		}

		return bufsize;
	}

	/**
	 * Copies inbuf to outpuf, assuming that inbuf is really a buffer of
	 * sized_sampleT.
	 * As a side effect, m_max_amplitude, m_average_sum and m_average_count are
	 * modified.
	 **/
	template<typename sized_sampleT>
	void copyBuffer(FLAC__int32 * outbuf, char * inbuf, int inbufsize) {
		sized_sampleT * inbuf_sized = reinterpret_cast<sized_sampleT *>(inbuf);
		for (int i = 0; i < inbufsize / sizeof(sized_sampleT); ++i) {
			sized_sampleT cur = inbuf_sized[i];

			// Convert sized sample to int32
			outbuf[i] = cur;

			// Convert to float on a range from 0..1
			if (cur < 0) {
				// Need to lose precision here, the positive value range is lower than
				// the negative value range in a signed integer.
				cur = -(cur + 1);
			}
			float amp = static_cast<float>(cur)
					/ type_traits<sized_sampleT>::MAX;

			// Store max amplitude
			if (amp > m_max_amplitude) {
				m_max_amplitude = amp;
			}

			// Sum average.
			if (!(i % m_channels)) {
				m_average_sum += amp;
				++m_average_count;
			}
		}
	}

	static void * trampoline_func(void * args);

	// Configuration values passed to ctor
	char * m_outfile;
	int m_sample_rate;
	int m_channels;
	int m_bits_per_sample;

	// FLAC encoder instance
	FLAC__StreamEncoder * m_encoder;

	// Max amplitude measured
	float m_max_amplitude;
	float m_average_sum;
	int m_average_count;

	// JNI thread's buffer.
	FLAC__int32 * m_write_buffer;
	int m_write_buffer_size;
	int m_write_buffer_offset;

	bool m_verify;
	int m_frame_size;


	// Write FIFO
	volatile write_fifo_t * m_fifo;
	pthread_mutex_t m_fifo_mutex;

	// Writer thread
	pthread_t m_writer;
	pthread_cond_t m_writer_condition;
	volatile bool m_kill_writer;

};

} // namespace

#endif