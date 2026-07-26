package com.englishcoach.app.engine.speech

import com.englishcoach.app.core.common.ModelEngine

/**
 * On-device speech-to-text. The production implementation is a whisper.cpp JNI bridge
 * (`WhisperSpeechRecognizer` in :engine:whisper); [FakeSpeechRecognizer] is used in tests
 * and wherever the real engine isn't wired in.
 */
interface SpeechRecognizer : ModelEngine {
    suspend fun transcribe(audioPcm16: ShortArray, sampleRateHz: Int = 16_000): TranscriptionResult
}

data class TranscriptionResult(
    val text: String,
    val confidence: Float,
    val words: List<WordTiming> = emptyList(),
)

/** [confidence] is the model's own average per-token probability for this word, in 0f..1f. */
data class WordTiming(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val confidence: Float = 1f,
)
