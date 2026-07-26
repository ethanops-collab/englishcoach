package com.englishcoach.app.engine.whisper

/** Thin JNI surface over whisper.cpp's C API (see `src/main/cpp/jni_bridge.cpp`). */
internal object WhisperNative {
    init {
        System.loadLibrary("whisper_jni")
    }

    /** Returns a native `whisper_context*` (as a jlong), or 0 on failure. */
    external fun nativeInit(modelPath: String): Long

    /** [samples] must be mono PCM in [-1.0, 1.0] at 16 kHz. */
    external fun nativeTranscribe(ctxPtr: Long, samples: FloatArray, nThreads: Int): String

    external fun nativeRelease(ctxPtr: Long)
}
