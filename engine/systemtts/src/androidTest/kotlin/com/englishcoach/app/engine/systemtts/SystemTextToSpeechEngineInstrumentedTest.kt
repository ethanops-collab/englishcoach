package com.englishcoach.app.engine.systemtts

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.englishcoach.app.core.common.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sqrt

/**
 * Real, on-device proof that Android's system TextToSpeech is actually wired up: initializes
 * the engine, synthesizes a real phrase, and checks the resulting PCM is structurally real
 * audio (non-empty, sane sample rate, energy above a noise floor) - the honest equivalent of
 * "does this actually work" given a test can't literally listen to it, matching the rigor of
 * the whisper.cpp/llama.cpp verification tests. No model file to push - unlike STT/LLM, this
 * engine uses whatever TTS voice data is already installed on the device.
 */
@RunWith(AndroidJUnit4::class)
class SystemTextToSpeechEngineInstrumentedTest {

    @Test
    fun synthesizesRealNonSilentAudio() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = SystemTextToSpeechEngine(context)

        val loadResult = engine.load("")
        assertTrue("TTS engine failed to initialize: $loadResult", loadResult is AppResult.Success)

        val audio = engine.synthesize("Hello, how are you today?")
        val durationSeconds = audio.pcm16.size.toFloat() / audio.sampleRateHz
        Log.i(
            "SystemTtsTest",
            "Generated ${audio.pcm16.size} samples at ${audio.sampleRateHz}Hz (${durationSeconds}s)",
        )

        assertTrue("Expected non-empty audio", audio.pcm16.isNotEmpty())
        assertTrue("Expected a sane sample rate, got ${audio.sampleRateHz}", audio.sampleRateHz in 8_000..48_000)

        val rms = sqrt(
            audio.pcm16.sumOf { sample ->
                val normalized = sample.toDouble() / Short.MAX_VALUE
                normalized * normalized
            } / audio.pcm16.size,
        )
        Log.i("SystemTtsTest", "RMS energy: $rms")
        assertTrue("Expected non-silent audio, RMS was too low: $rms", rms > 0.01)

        engine.unload()
    }
}
