package com.englishcoach.app.core.common

import kotlinx.coroutines.flow.StateFlow

enum class ModelState { UNLOADED, LOADING, READY, ERROR }

/**
 * Shared lifecycle contract for every on-device model engine (STT, LLM, TTS). Model weight
 * files are never bundled in the APK or fetched implicitly — callers decide when to load a
 * model from an on-device path and must observe [state] before using the engine.
 */
interface ModelEngine {
    val state: StateFlow<ModelState>
    suspend fun load(modelPath: String): AppResult<Unit>
    fun unload()
}
