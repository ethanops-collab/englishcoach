package com.englishcoach.app.engine.pronunciation

import com.englishcoach.app.core.model.PronunciationScore
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Real (non-random) pronunciation scorer built entirely from signals the on-device STT
 * engine actually produces: which reference words got recognized at all, the model's own
 * per-word confidence ([RecognizedWord.confidence]), and word timing.
 *
 * Honest about its limits: this engine receives word-level STT output, not raw audio, so it
 * cannot do true phoneme-level acoustic analysis (that would need a dedicated
 * forced-alignment/GOP model - a separate, much larger undertaking). Concretely:
 * - [PronunciationScore.accuracy] is real: word-match ratio against [referenceText] blended
 *   with the STT model's own confidence for the words it did recognize.
 * - [PronunciationScore.rhythm] is real: consistency of word durations from actual timing
 *   data (evenly-paced speech scores higher than very uneven pacing).
 * - [PronunciationScore.stress] / [PronunciationScore.intonation] have no real per-utterance
 *   signal available at this layer (no pitch/F0 data reaches this class) - they're a
 *   deterministic function of the two real scores above, not random, but they are an
 *   approximation, not an independent measurement.
 * - [PronunciationScore.problemSoundScores]: for sounds with a clear text-level indicator
 *   (e.g. TH -> words containing "th"), the score is real - it's the confidence/match rate
 *   restricted to just the reference words that exercise that sound. For sounds with no
 *   reliable text indicator (linking, stress, short vowels), it falls back to the overall
 *   accuracy score rather than fabricating a specific number.
 */
class WhisperConfidencePronunciationScorer @Inject constructor() : PronunciationScorer {

    override suspend fun score(
        referenceText: String,
        recognizedWords: List<RecognizedWord>,
        nativeLanguageTag: String,
    ): PronunciationScore {
        val referenceWords = normalizeWords(referenceText)
        val recognized = recognizedWords.map { it.copy(word = normalize(it.word)) }

        val alignment = alignWords(referenceWords, recognized.map { it.word })

        var matchedCount = 0
        var confidenceSum = 0f
        for (recIndex in alignment) {
            if (recIndex != null) {
                matchedCount++
                confidenceSum += recognized[recIndex].confidence
            }
        }
        val wordMatchRatio = if (referenceWords.isNotEmpty()) matchedCount.toFloat() / referenceWords.size else 0f
        val avgConfidence = if (matchedCount > 0) confidenceSum / matchedCount else 0f
        val accuracy = ((wordMatchRatio * 0.6f + avgConfidence * 0.4f) * 100f).coerceIn(0f, 100f)

        val rhythm = computeRhythmScore(recognizedWords)

        // No pitch/prosody data reaches this layer - approximate from the two real scores above.
        val stress = (accuracy * 0.5f + rhythm * 0.5f).coerceIn(0f, 100f)
        val intonation = (accuracy * 0.3f + rhythm * 0.7f).coerceIn(0f, 100f)

        val overall = (accuracy + rhythm + stress + intonation) / 4f

        val problemSounds = NativeLanguageProblemSoundProfiles.forNativeLanguage(nativeLanguageTag)
        val problemSoundScores = problemSounds.associate { sound ->
            sound.name to scoreProblemSound(sound, referenceWords, alignment, recognized, accuracy)
        }
        val missingSounds = problemSoundScores.filterValues { it < 55f }.keys.toList()

        return PronunciationScore(
            overall = overall,
            accuracy = accuracy,
            stress = stress,
            rhythm = rhythm,
            intonation = intonation,
            missingSounds = missingSounds,
            problemSoundScores = problemSoundScores,
        )
    }

    /** Coefficient-of-variation of word durations, mapped so more even pacing scores higher. */
    private fun computeRhythmScore(words: List<RecognizedWord>): Float {
        val durations = words.map { max(1L, it.endMs - it.startMs).toFloat() }
        if (durations.size < 2) return if (durations.isEmpty()) 0f else 70f

        val mean = durations.average().toFloat()
        val variance = durations.sumOf { d -> ((d - mean) * (d - mean)).toDouble() }.toFloat() / durations.size
        val stdDev = sqrt(variance)
        val coefficientOfVariation = if (mean > 0f) stdDev / mean else 1f

        // A CoV of 0 (perfectly even) -> 100; a CoV of 1.0+ (very uneven) -> floor around 40.
        return (100f - coefficientOfVariation * 60f).coerceIn(40f, 100f)
    }

    private fun scoreProblemSound(
        sound: ProblemSound,
        referenceWords: List<String>,
        alignment: List<Int?>,
        recognized: List<RecognizedWord>,
        overallAccuracyFallback: Float,
    ): Float {
        val indicator = SOUND_INDICATORS[sound] ?: return overallAccuracyFallback

        var relevantCount = 0
        var relevantConfidenceSum = 0f
        for ((refIndex, refWord) in referenceWords.withIndex()) {
            if (!indicator(refWord)) continue
            relevantCount++
            val recIndex = alignment[refIndex]
            relevantConfidenceSum += if (recIndex != null) recognized[recIndex].confidence else 0f
        }

        if (relevantCount == 0) return overallAccuracyFallback
        return ((relevantConfidenceSum / relevantCount) * 100f).coerceIn(0f, 100f)
    }

    private fun normalizeWords(text: String): List<String> =
        text.split(Regex("\\s+")).map { normalize(it) }.filter { it.isNotBlank() }

    private fun normalize(word: String): String =
        word.lowercase().trim { !it.isLetter() }

    /**
     * Greedy left-to-right alignment: for each reference word, search forward in the
     * recognized list (within a small lookahead window) for an exact or near match. Returns,
     * per reference word index, the matched recognized-word index or null if unmatched.
     * Deliberately simple (not full edit-distance DP) since lesson utterances are short.
     */
    private fun alignWords(referenceWords: List<String>, recognizedWords: List<String>): List<Int?> {
        val result = MutableList<Int?>(referenceWords.size) { null }
        var searchFrom = 0
        for ((i, refWord) in referenceWords.withIndex()) {
            var bestIndex: Int? = null
            var bestDistance = Int.MAX_VALUE
            val windowEnd = min(recognizedWords.size, searchFrom + LOOKAHEAD_WINDOW)
            for (j in searchFrom until windowEnd) {
                val distance = levenshtein(refWord, recognizedWords[j])
                val threshold = max(1, refWord.length / 3)
                if (distance <= threshold && distance < bestDistance) {
                    bestDistance = distance
                    bestIndex = j
                }
            }
            if (bestIndex != null) {
                result[i] = bestIndex
                searchFrom = bestIndex + 1
            }
        }
        return result
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            for (j in 0..b.length) prev[j] = curr[j]
        }
        return prev[b.length]
    }

    private companion object {
        const val LOOKAHEAD_WINDOW = 4

        val SOUND_INDICATORS: Map<ProblemSound, (String) -> Boolean> = mapOf(
            ProblemSound.L_R to { w -> w.contains('l') || w.contains('r') },
            ProblemSound.R to { w -> w.contains('r') },
            ProblemSound.TH to { w -> w.contains("th") },
            ProblemSound.V_B to { w -> w.contains('v') || w.contains('b') },
            ProblemSound.F_P to { w -> w.contains('f') || w.contains('p') },
            ProblemSound.V_W to { w -> w.contains('v') || w.contains('w') },
            ProblemSound.H to { w -> w.contains('h') },
            ProblemSound.R_ENDING to { w -> w.endsWith('r') },
            ProblemSound.CONSONANT_ENDINGS to { w -> w.isNotEmpty() && w.last() !in "aeiou" },
            // LINKING, SHORT_VOWELS, STRESS have no reliable text-level indicator - they fall
            // back to the overall accuracy score in scoreProblemSound rather than being
            // fabricated here.
        )
    }
}
