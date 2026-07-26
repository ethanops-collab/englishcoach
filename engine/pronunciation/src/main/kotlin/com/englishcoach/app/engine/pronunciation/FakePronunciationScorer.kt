package com.englishcoach.app.engine.pronunciation

import com.englishcoach.app.core.model.PronunciationScore
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

/**
 * Produces plausible-looking scores, deliberately scoring the speaker's native-language
 * problem sounds lower than the rest so the weak-pronunciation UI has something real to
 * show while ONNX/real acoustic scoring isn't wired in yet.
 */
class FakePronunciationScorer @Inject constructor() : PronunciationScorer {

    override suspend fun score(
        referenceText: String,
        recognizedWords: List<RecognizedWord>,
        nativeLanguageTag: String,
    ): PronunciationScore {
        delay(250)
        val problemSounds = NativeLanguageProblemSoundProfiles.forNativeLanguage(nativeLanguageTag)
        val problemSoundScores = problemSounds.associate { sound ->
            sound.name to Random.nextFloat().let { 45f + it * 25f }
        }
        val accuracy = 78f + Random.nextFloat() * 15f
        val stress = 70f + Random.nextFloat() * 20f
        val rhythm = 72f + Random.nextFloat() * 18f
        val intonation = 70f + Random.nextFloat() * 20f
        val overall = (accuracy + stress + rhythm + intonation) / 4f

        return PronunciationScore(
            overall = overall,
            accuracy = accuracy,
            stress = stress,
            rhythm = rhythm,
            intonation = intonation,
            missingSounds = problemSounds.filter { (problemSoundScores[it.name] ?: 100f) < 55f }.map { it.name },
            problemSoundScores = problemSoundScores,
        )
    }
}
