package com.englishcoach.app.feature.lesson.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SAMPLE_RATE_HZ = 16_000

/**
 * Thin wrapper around [AudioRecord] for push-to-talk capture. Real mic capture even though
 * the STT behind it is currently [com.englishcoach.app.engine.speech.FakeSpeechRecognizer] -
 * this piece needs no native AI library and works standalone. Reads block on the IO
 * dispatcher inside a cancellable loop so [stopAndGetPcm16] returns everything captured
 * since [start], not just whatever happened to be buffered at that instant.
 */
class MicAudioCapture @Inject constructor() {

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private val chunks = mutableListOf<Short>()

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(1_024)

        synchronized(chunks) { chunks.clear() }

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2,
        )
        audioRecord = record
        record.startRecording()

        captureJob = scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(2_048)
            while (isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    synchronized(chunks) {
                        for (i in 0 until read) chunks.add(buffer[i])
                    }
                } else {
                    break
                }
            }
        }
    }

    suspend fun stopAndGetPcm16(): ShortArray {
        captureJob?.cancelAndJoin()
        captureJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        return synchronized(chunks) { chunks.toShortArray() }
    }

    fun sampleRateHz(): Int = SAMPLE_RATE_HZ
}
