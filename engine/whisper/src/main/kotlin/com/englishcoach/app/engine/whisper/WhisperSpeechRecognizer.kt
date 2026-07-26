package com.englishcoach.app.engine.whisper

import android.content.Context
import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import com.englishcoach.app.engine.speech.SpeechRecognizer
import com.englishcoach.app.engine.speech.TranscriptionResult
import com.englishcoach.app.engine.speech.WordTiming
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Real whisper.cpp-backed [SpeechRecognizer]. Wraps the JNI bridge in [WhisperNative].
 * The model is never bundled in the APK or auto-downloaded - it must already exist at
 * [defaultModelPath] (pushed there manually for now; a proper in-app acquisition flow is
 * a follow-up). Uses internal storage (`context.filesDir`), not external - some emulator
 * images don't reliably expose `getExternalFilesDir()`-pushed files back to the app process
 * (verified empirically while testing this module). [transcribe] lazily loads the model on
 * first use so callers never need to call [load] explicitly.
 */
class WhisperSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechRecognizer {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    private val loadMutex = Mutex()
    private var ctxPtr: Long = 0L

    override suspend fun load(modelPath: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        loadMutex.withLock {
            if (ctxPtr != 0L) return@withLock AppResult.Success(Unit)

            _state.value = ModelState.LOADING
            val file = File(modelPath)
            if (!file.exists()) {
                _state.value = ModelState.ERROR
                return@withLock AppResult.Error(message = "Whisper model not found at $modelPath")
            }

            val ptr = WhisperNative.nativeInit(modelPath)
            if (ptr == 0L) {
                _state.value = ModelState.ERROR
                AppResult.Error(message = "whisper_init_from_file_with_params failed for $modelPath")
            } else {
                ctxPtr = ptr
                _state.value = ModelState.READY
                AppResult.Success(Unit)
            }
        }
    }

    override fun unload() {
        if (ctxPtr != 0L) {
            WhisperNative.nativeRelease(ctxPtr)
            ctxPtr = 0L
        }
        _state.value = ModelState.UNLOADED
    }

    override suspend fun transcribe(audioPcm16: ShortArray, sampleRateHz: Int): TranscriptionResult {
        check(sampleRateHz == WHISPER_SAMPLE_RATE_HZ) {
            "WhisperSpeechRecognizer only supports ${WHISPER_SAMPLE_RATE_HZ}Hz audio, got ${sampleRateHz}Hz"
        }
        if (ctxPtr == 0L) {
            val result = load(defaultModelPath())
            if (result is AppResult.Error) {
                error(result.message ?: "Failed to load Whisper model")
            }
        }

        return withContext(Dispatchers.Default) {
            val samples = FloatArray(audioPcm16.size) { i -> audioPcm16[i] / 32768f }
            val nThreads = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)
            val raw = WhisperNative.nativeTranscribe(ctxPtr, samples, nThreads)
            parseResult(raw)
        }
    }

    /**
     * Parses the `text<SECTION_SEP>word<FIELD_SEP>start<FIELD_SEP>end<FIELD_SEP>confidence
     * <WORD_SEP>word<FIELD_SEP>...` format encoded by `jni_bridge.cpp`'s `encode_words` - see
     * that file for why the encoding exists (no dedicated word-level API in whisper.cpp, so
     * word boundaries + per-word confidence are computed there from token-level data and
     * passed back as one delimited string).
     */
    private fun parseResult(raw: String): TranscriptionResult {
        val sectionParts = raw.split(SECTION_SEP, limit = 2)
        val text = sectionParts.getOrElse(0) { "" }.trim()
        val wordsRaw = sectionParts.getOrElse(1) { "" }

        val words = wordsRaw.split(WORD_SEP)
            .filter { it.isNotBlank() }
            .mapNotNull { record ->
                val fields = record.split(FIELD_SEP)
                if (fields.size < 4) return@mapNotNull null
                WordTiming(
                    word = fields[0],
                    startMs = fields[1].toLongOrNull() ?: 0L,
                    endMs = fields[2].toLongOrNull() ?: 0L,
                    confidence = fields[3].toFloatOrNull() ?: 0f,
                )
            }

        val overallConfidence = if (words.isNotEmpty()) words.map { it.confidence }.average().toFloat() else 0f
        return TranscriptionResult(text = text, confidence = overallConfidence, words = words)
    }

    private fun defaultModelPath(): String =
        File(context.filesDir, "models/$DEFAULT_MODEL_FILE_NAME").absolutePath

    private companion object {
        const val WHISPER_SAMPLE_RATE_HZ = 16_000
        const val DEFAULT_MODEL_FILE_NAME = "ggml-tiny.en.bin"

        // Must match the FIELD_SEP/WORD_SEP/SECTION_SEP constants in jni_bridge.cpp exactly
        // (ASCII unit/record/group separators - non-printable, so built via Char(Int) rather
        // than an embedded literal; not `const` since Char(Int) isn't a compile-time constant).
        val FIELD_SEP: Char = Char(0x1F)
        val WORD_SEP: Char = Char(0x1E)
        val SECTION_SEP: Char = Char(0x1D)
    }
}
