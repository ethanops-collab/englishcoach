package com.englishcoach.app.core.model

/**
 * All sub-scores are 0f..100f. [problemSoundScores] is keyed by the phoneme identifiers
 * defined in the pronunciation engine (e.g. "L_R", "TH") so it stays independent of any
 * particular native-language rule set.
 */
data class PronunciationScore(
    val overall: Float,
    val accuracy: Float,
    val stress: Float,
    val rhythm: Float,
    val intonation: Float,
    val missingSounds: List<String>,
    val problemSoundScores: Map<String, Float>,
)
