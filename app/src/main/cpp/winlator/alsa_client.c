#include <aaudio/AAudio.h>
#include <jni.h>
#include <android/log.h>

#define WAIT_COMPLETION_TIMEOUT 100 * 1000000L
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, "System.out", __VA_ARGS__);

enum Format {U8, S16LE, S16BE, FLOATLE, FLOATBE};

// Helper functions for pointer conversion
static inline jlong ptr_to_jlong(void* ptr) {
    return (jlong)(uintptr_t)ptr;
}
static inline void* jlong_to_ptr(jlong handle) {
    return (void*)(uintptr_t)handle;
}

static aaudio_format_t toAAudioFormat(int format) {
    switch (format) {
        case FLOATLE:
        case FLOATBE:
            return AAUDIO_FORMAT_PCM_FLOAT;
        case U8:
            return AAUDIO_FORMAT_UNSPECIFIED;
        case S16LE:
        case S16BE:
        default:
            return AAUDIO_FORMAT_PCM_I16;
    }
}

static AAudioStream *aaudioCreate(int32_t format, int8_t channelCount, int32_t sampleRate, int32_t bufferSize) {
    aaudio_result_t result;
    AAudioStreamBuilder *builder;
    AAudioStream *stream;

    result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) return NULL;

    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setFormat(builder, toAAudioFormat(format));
    AAudioStreamBuilder_setChannelCount(builder, channelCount);
    AAudioStreamBuilder_setSampleRate(builder, sampleRate);

    result = AAudioStreamBuilder_openStream(builder, &stream);
    if (result != AAUDIO_OK) {
        printf("Failed to open AAudio stream");
        AAudioStreamBuilder_delete(builder);
        return NULL;
    } else printf("Opened AAudio stream successfully");

    AAudioStream_setBufferSizeInFrames(stream, bufferSize);

    result = AAudioStreamBuilder_delete(builder);
    if (result != AAUDIO_OK) return NULL;

    return stream;
}

static int aaudioWrite(AAudioStream *aaudioStream, void *buffer, int numFrames) {
    aaudio_result_t framesWritten = AAudioStream_write(aaudioStream, buffer, numFrames, WAIT_COMPLETION_TIMEOUT);
    return framesWritten;
}

static void aaudioStart(AAudioStream *aaudioStream) {
    AAudioStream_requestStart(aaudioStream);
    AAudioStream_waitForStateChange(aaudioStream, AAUDIO_STREAM_STATE_STARTING, NULL, WAIT_COMPLETION_TIMEOUT);
}

static void aaudioStop(AAudioStream *aaudioStream) {
    AAudioStream_requestStop(aaudioStream);
    AAudioStream_waitForStateChange(aaudioStream, AAUDIO_STREAM_STATE_STOPPING, NULL, WAIT_COMPLETION_TIMEOUT);
}

static void aaudioPause(AAudioStream *aaudioStream) {
    AAudioStream_requestPause(aaudioStream);
    AAudioStream_waitForStateChange(aaudioStream, AAUDIO_STREAM_STATE_PAUSING, NULL, WAIT_COMPLETION_TIMEOUT);
}

static void aaudioFlush(AAudioStream *aaudioStream) {
    AAudioStream_requestFlush(aaudioStream);
    AAudioStream_waitForStateChange(aaudioStream, AAUDIO_STREAM_STATE_FLUSHING, NULL, WAIT_COMPLETION_TIMEOUT);
}

//JNIEXPORT jlong JNICALL
//Java_com_winlator_alsaserver_ALSAClient_create(JNIEnv *env, jobject obj, jint format,
//                                               jbyte channelCount, jint sampleRate, jint bufferSize) {
////    long streamPtr = (long)aaudioCreate(format, channelCount, sampleRate, bufferSize);
////    printf("AAudioCreate streamPtr %ld", streamPtr);
////    return streamPtr;
//    return (jlong)aaudioCreate(format, channelCount, sampleRate, bufferSize);
//}
JNIEXPORT jlong JNICALL
Java_com_winlator_alsaserver_ALSAClient_create(JNIEnv *env, jobject obj, jint format,
                                               jbyte channelCount, jint sampleRate, jint bufferSize) {
    void* stream = aaudioCreate(format, channelCount, sampleRate, bufferSize);
    // Add validation
    if (stream == NULL) {
        return 0; // Return 0 instead of NULL to maintain consistency with Java checks
    }
    return ptr_to_jlong(stream);
}

JNIEXPORT jint JNICALL
Java_com_winlator_alsaserver_ALSAClient_write(JNIEnv *env, jobject obj, jlong streamPtr, jobject buffer,
                                              jint numFrames) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) {
        return aaudioWrite(aaudioStream, (*env)->GetDirectBufferAddress(env, buffer), numFrames);
    }
    else return -1;
}

JNIEXPORT void JNICALL
Java_com_winlator_alsaserver_ALSAClient_start(JNIEnv *env, jobject obj, jlong streamPtr) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) aaudioStart(aaudioStream);
}

JNIEXPORT void JNICALL
Java_com_winlator_alsaserver_ALSAClient_stop(JNIEnv *env, jobject obj, jlong streamPtr) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) aaudioStop(aaudioStream);
}

JNIEXPORT void JNICALL
Java_com_winlator_alsaserver_ALSAClient_pause(JNIEnv *env, jobject obj, jlong streamPtr) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) aaudioPause(aaudioStream);
}

JNIEXPORT void JNICALL
Java_com_winlator_alsaserver_ALSAClient_flush(JNIEnv *env, jobject obj, jlong streamPtr) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) aaudioFlush(aaudioStream);
}

JNIEXPORT void JNICALL
Java_com_winlator_alsaserver_ALSAClient_close(JNIEnv *env, jobject obj, jlong streamPtr) {
    AAudioStream *aaudioStream = (AAudioStream*)jlong_to_ptr(streamPtr);
    if (aaudioStream) AAudioStream_close(aaudioStream);
}