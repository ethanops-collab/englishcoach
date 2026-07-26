package com.englishcoach.app.feature.lesson.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.englishcoach.app.engine.tts.SynthesizedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Plays a [SynthesizedAudio] buffer through [AudioTrack]. Silence is a valid input. */
class AudioPlayback @Inject constructor() {

    suspend fun play(audio: SynthesizedAudio) = withContext(Dispatchers.IO) {
        if (audio.pcm16.isEmpty()) return@withContext

        val bufferSizeInBytes = (audio.pcm16.size * 2).coerceAtLeast(
            AudioTrack.getMinBufferSize(audio.sampleRateHz, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
        )

        val track = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(audio.sampleRateHz)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bufferSizeInBytes,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )

        track.write(audio.pcm16, 0, audio.pcm16.size)
        track.play()
        val durationMs = (audio.pcm16.size.toLong() * 1000L) / audio.sampleRateHz
        delay(durationMs)
        track.stop()
        track.release()
    }
}
