package com.englishcoach.app.engine.tts

import com.englishcoach.app.core.common.ModelEngine

/**
 * On-device speech synthesis. The production implementation (`SystemTextToSpeechEngine` in
 * :engine:systemtts) wraps Android's built-in `android.speech.tts.TextToSpeech` rather than
 * Piper - Piper's phoneme quality depends on espeak-ng (GPL-3), which would obligate this
 * app's distributed source under GPL-3; the system TTS engine is still 100% on-device with
 * no such licensing cost. See CLAUDE.md's "On-device AI stack" section.
 */
interface TextToSpeechEngine : ModelEngine {
    suspend fun synthesize(text: String): SynthesizedAudio
}

/** Raw 16-bit PCM mono audio ready for AudioTrack playback. */
data class SynthesizedAudio(
    val pcm16: ShortArray,
    val sampleRateHz: Int,
)
