package com.englishcoach.app.engine.pronunciation

import com.englishcoach.app.core.model.PronunciationScore

/** [confidence] is the STT engine's own per-word confidence (0f..1f), when available. */
data class RecognizedWord(val word: String, val startMs: Long, val endMs: Long, val confidence: Float = 1f)

/**
 * Scores a spoken attempt against the expected [referenceText], weighting the phonemes in
 * [NativeLanguageProblemSoundProfiles] for the speaker's native language more heavily.
 * Deliberately independent of :engine:speech — it consumes plain word timings, not a
 * specific STT engine's types.
 */
interface PronunciationScorer {
    suspend fun score(
        referenceText: String,
        recognizedWords: List<RecognizedWord>,
        nativeLanguageTag: String,
    ): PronunciationScore
}
