package com.englishcoach.app.engine.llama

/** Thin JNI surface over llama.cpp's core C API (see `src/main/cpp/jni_bridge.cpp`). */
internal object LlamaNative {
    init {
        System.loadLibrary("llama_jni")
    }

    /** Returns an opaque native handle (model+context), or 0 on failure. */
    external fun nativeInit(modelPath: String): Long

    /**
     * [roles]/[contents] are parallel arrays (each element is one chat turn, "user" or
     * "assistant"). Rebuilds the full prompt from [systemPrompt] + these turns every call.
     */
    external fun nativeComplete(
        handle: Long,
        systemPrompt: String,
        roles: Array<String>,
        contents: Array<String>,
        maxTokens: Int,
        temperature: Float,
    ): String

    external fun nativeRelease(handle: Long)
}
