package com.englishcoach.app.engine.llama

import android.content.Context
import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import com.englishcoach.app.engine.llm.LlmCoach
import com.englishcoach.app.engine.llm.LlmRequest
import com.englishcoach.app.engine.llm.LlmResponse
import com.englishcoach.app.engine.llm.LlmRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

/**
 * Real llama.cpp-backed [LlmCoach]. Wraps the JNI bridge in [LlamaNative]. Same lazy-load
 * pattern as `WhisperSpeechRecognizer`: the model is never bundled or auto-downloaded, must
 * already exist at [defaultModelPath] (pushed there manually for now), and `complete()`
 * loads it on first use so callers never need to call [load] explicitly. Uses internal
 * storage (`context.filesDir`), not external - see `WhisperSpeechRecognizer`'s doc for why.
 */
class LlamaLlmCoach @Inject constructor(
    @ApplicationContext private val context: Context,
) : LlmCoach {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    private val loadMutex = Mutex()
    private var handle: Long = 0L

    override suspend fun load(modelPath: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        loadMutex.withLock {
            if (handle != 0L) return@withLock AppResult.Success(Unit)

            _state.value = ModelState.LOADING
            val file = File(modelPath)
            if (!file.exists()) {
                _state.value = ModelState.ERROR
                return@withLock AppResult.Error(message = "LLM model not found at $modelPath")
            }

            val ptr = LlamaNative.nativeInit(modelPath)
            if (ptr == 0L) {
                _state.value = ModelState.ERROR
                AppResult.Error(message = "llama_model_load_from_file failed for $modelPath")
            } else {
                handle = ptr
                _state.value = ModelState.READY
                AppResult.Success(Unit)
            }
        }
    }

    override fun unload() {
        if (handle != 0L) {
            LlamaNative.nativeRelease(handle)
            handle = 0L
        }
        _state.value = ModelState.UNLOADED
    }

    override suspend fun complete(request: LlmRequest): LlmResponse {
        if (handle == 0L) {
            val result = load(defaultModelPath())
            if (result is AppResult.Error) {
                error(result.message ?: "Failed to load LLM model")
            }
        }

        return withContext(Dispatchers.Default) {
            val roles = request.messages.map { if (it.role == LlmRole.USER) "user" else "assistant" }.toTypedArray()
            val contents = request.messages.map { it.content }.toTypedArray()
            val text = LlamaNative.nativeComplete(
                handle,
                request.systemPrompt,
                roles,
                contents,
                request.maxTokens,
                request.temperature,
            ).trim()
            LlmResponse(text = text, tokensGenerated = text.length / 4)
        }
    }

    private fun defaultModelPath(): String =
        File(context.filesDir, "models/$DEFAULT_MODEL_FILE_NAME").absolutePath

    private companion object {
        // Production model per CLAUDE.md's spec (Gemma 3 4B), not the small Qwen2.5 0.5B used
        // to verify the pipeline quickly. Swapping models later is just this one filename.
        const val DEFAULT_MODEL_FILE_NAME = "gemma-3-4b-it-Q4_K_M.gguf"
    }
}
