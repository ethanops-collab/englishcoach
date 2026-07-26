package com.englishcoach.app.engine.systemtts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import com.englishcoach.app.engine.tts.SynthesizedAudio
import com.englishcoach.app.engine.tts.TextToSpeechEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Real [TextToSpeechEngine] backed by Android's built-in on-device TTS engine. Android's
 * `TextToSpeech` plays audio directly rather than returning samples, so [synthesize] renders
 * to a temp WAV via `synthesizeToFile` and reads the PCM16 data back out, matching the
 * [SynthesizedAudio] contract the rest of the app (AudioTrack playback in :feature:lesson)
 * expects.
 */
class SystemTextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextToSpeechEngine {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    private var tts: TextToSpeech? = null

    override suspend fun load(modelPath: String): AppResult<Unit> {
        if (tts != null) return AppResult.Success(Unit)

        _state.value = ModelState.LOADING
        return suspendCancellableCoroutine { cont ->
            var engine: TextToSpeech? = null
            engine = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engine?.language = Locale.US
                    tts = engine
                    _state.value = ModelState.READY
                    if (cont.isActive) cont.resume(AppResult.Success(Unit))
                } else {
                    _state.value = ModelState.ERROR
                    if (cont.isActive) {
                        cont.resume(AppResult.Error(message = "TextToSpeech init failed: status=$status"))
                    }
                }
            }
        }
    }

    override fun unload() {
        tts?.shutdown()
        tts = null
        _state.value = ModelState.UNLOADED
    }

    override suspend fun synthesize(text: String): SynthesizedAudio {
        if (tts == null) {
            val result = load("")
            if (result is AppResult.Error) {
                error(result.message ?: "Failed to initialize TextToSpeech")
            }
        }
        val engine = requireNotNull(tts)

        val outFile = File(context.cacheDir, "tts_${UUID.randomUUID()}.wav")
        val utteranceId = outFile.name

        suspendCancellableCoroutine<Unit> { cont ->
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) = Unit

                override fun onDone(id: String?) {
                    if (id == utteranceId && cont.isActive) cont.resume(Unit)
                }

                @Deprecated("Deprecated in Java", ReplaceWith(""))
                override fun onError(id: String?) {
                    if (id == utteranceId && cont.isActive) {
                        cont.resumeWithException(IllegalStateException("TextToSpeech synthesis failed for $id"))
                    }
                }
            })

            val result = engine.synthesizeToFile(text, Bundle(), outFile, utteranceId)
            if (result != TextToSpeech.SUCCESS && cont.isActive) {
                cont.resumeWithException(IllegalStateException("synthesizeToFile returned $result"))
            }
        }

        val audio = readWavPcm16(outFile)
        outFile.delete()
        return audio
    }

    private fun readWavPcm16(file: File): SynthesizedAudio {
        val bytes = file.readBytes()
        var offset = 12 // past "RIFF"+size+"WAVE"
        var sampleRateHz = 22_050
        var dataOffset = -1
        var dataSize = 0
        while (offset + 8 <= bytes.size) {
            val chunkId = String(bytes, offset, 4, Charsets.US_ASCII)
            val chunkSize = ByteBuffer.wrap(bytes, offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            when (chunkId) {
                "fmt " -> {
                    sampleRateHz = ByteBuffer.wrap(bytes, offset + 12, 4).order(ByteOrder.LITTLE_ENDIAN).int
                }
                "data" -> {
                    dataOffset = offset + 8
                    dataSize = chunkSize
                }
            }
            offset += 8 + chunkSize + (chunkSize % 2)
        }
        check(dataOffset >= 0) { "No 'data' chunk found in synthesized WAV" }

        val sampleCount = dataSize / 2
        val samples = ShortArray(sampleCount)
        val buffer = ByteBuffer.wrap(bytes, dataOffset, dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until sampleCount) samples[i] = buffer.short

        return SynthesizedAudio(pcm16 = samples, sampleRateHz = sampleRateHz)
    }
}
