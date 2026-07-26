package com.englishcoach.app.engine.speech

import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Deterministic stand-in for whisper.cpp used by the rest of the app (and its tests) until
 * the native module is integrated. Always "hears" the same rotating set of scripted lines
 * so the full lesson flow is exercisable end to end.
 */
class FakeSpeechRecognizer @Inject constructor() : SpeechRecognizer {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    private val scriptedLines = listOf(
        "I goed to school yesterday.",
        "Can I have a coffee, please.",
        "I want to book a room for two night.",
        "She don't like spicy food.",
    )
    private var callCount = 0

    override suspend fun load(modelPath: String): AppResult<Unit> {
        _state.value = ModelState.LOADING
        delay(150)
        _state.value = ModelState.READY
        return AppResult.Success(Unit)
    }

    override fun unload() {
        _state.value = ModelState.UNLOADED
    }

    override suspend fun transcribe(audioPcm16: ShortArray, sampleRateHz: Int): TranscriptionResult {
        delay(300)
        val line = scriptedLines[callCount % scriptedLines.size]
        callCount++
        return TranscriptionResult(text = line, confidence = 0.92f)
    }
}
