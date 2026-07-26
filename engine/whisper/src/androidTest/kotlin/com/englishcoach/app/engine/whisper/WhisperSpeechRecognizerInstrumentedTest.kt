package com.englishcoach.app.engine.whisper

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.englishcoach.app.core.common.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Real, on-device proof that whisper.cpp is actually wired up: transcribes whisper.cpp's own
 * canonical test clip (JFK's "ask not what your country can do for you") and checks the
 * output contains a recognizable word. Requires `ggml-tiny.en.bin` and `jfk.wav` to already be
 * pushed to this app's external files dir (see the verification steps in the project plan) -
 * this test does not download them itself, matching the "models are never fetched silently"
 * architecture rule.
 */
@RunWith(AndroidJUnit4::class)
class WhisperSpeechRecognizerInstrumentedTest {

    @Test
    fun transcribesJfkSampleAndContainsExpectedWord() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Internal storage (context.filesDir), not external - avoids scoped-storage/FUSE
        // permission quirks when pushing fixtures onto the emulator via `run-as` for testing.
        val modelFile = File(context.filesDir, "models/ggml-tiny.en.bin")
        val wavFile = File(context.filesDir, "test-audio/jfk.wav")

        assertTrue(
            "Model file must be pushed to ${modelFile.absolutePath} before running this test",
            modelFile.exists(),
        )
        assertTrue(
            "jfk.wav must be pushed to ${wavFile.absolutePath} before running this test",
            wavFile.exists(),
        )

        val pcm16 = readWavPcm16(wavFile)

        val recognizer = WhisperSpeechRecognizer(context)
        val loadResult = recognizer.load(modelFile.absolutePath)
        assertTrue("Model failed to load: $loadResult", loadResult is AppResult.Success)

        val result = recognizer.transcribe(pcm16, sampleRateHz = 16_000)
        Log.i("WhisperTest", "Transcribed text: \"${result.text}\"")

        assertTrue(
            "Expected the transcription to mention \"country\", got: \"${result.text}\"",
            result.text.contains("country", ignoreCase = true),
        )

        // Real per-word confidence/timing (added for on-device pronunciation scoring) - not
        // hand-crafted test data, this is whatever whisper.cpp's token_timestamps actually
        // produced for this clip.
        assertTrue("Expected non-empty word-level timing data", result.words.isNotEmpty())
        for (word in result.words.take(10)) {
            Log.i(
                "WhisperTest",
                "  word=\"${word.word}\" start=${word.startMs}ms end=${word.endMs}ms confidence=${word.confidence}",
            )
        }
        assertTrue(
            "Expected at least one word with meaningful confidence",
            result.words.any { it.confidence > 0.3f },
        )
    }

    /** Minimal PCM16 WAV reader: scans chunks for "data" rather than assuming a fixed header size. */
    private fun readWavPcm16(file: File): ShortArray {
        val bytes = file.readBytes()
        var offset = 12 // past "RIFF"+size+"WAVE"
        var dataOffset = -1
        var dataSize = 0
        while (offset + 8 <= bytes.size) {
            val chunkId = String(bytes, offset, 4, Charsets.US_ASCII)
            val chunkSize = ByteBuffer.wrap(bytes, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (chunkId == "data") {
                dataOffset = offset + 8
                dataSize = chunkSize
                break
            }
            offset += 8 + chunkSize + (chunkSize % 2)
        }
        check(dataOffset >= 0) { "No 'data' chunk found in ${file.name}" }

        val sampleCount = dataSize / 2
        val samples = ShortArray(sampleCount)
        val buffer = ByteBuffer.wrap(bytes, dataOffset, dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until sampleCount) samples[i] = buffer.short
        return samples
    }
}
