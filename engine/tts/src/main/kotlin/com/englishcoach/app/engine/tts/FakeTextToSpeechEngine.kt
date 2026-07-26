package com.englishcoach.app.engine.tts

import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Returns a short burst of silent PCM instead of real speech so playback wiring is testable. */
class FakeTextToSpeechEngine @Inject constructor() : TextToSpeechEngine {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    override suspend fun load(modelPath: String): AppResult<Unit> {
        _state.value = ModelState.LOADING
        delay(150)
        _state.value = ModelState.READY
        return AppResult.Success(Unit)
    }

    override fun unload() {
        _state.value = ModelState.UNLOADED
    }

    override suspend fun synthesize(text: String): SynthesizedAudio {
        delay(200)
        val sampleRateHz = 22_050
        val durationMs = (text.length * 60L).coerceIn(400L, 4_000L)
        val sampleCount = (sampleRateHz * durationMs / 1000L).toInt()
        return SynthesizedAudio(pcm16 = ShortArray(sampleCount), sampleRateHz = sampleRateHz)
    }
}
