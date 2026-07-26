package com.englishcoach.app.engine.pronunciation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class WhisperConfidencePronunciationScorerTest {

    private val scorer = WhisperConfidencePronunciationScorer()

    @Test
    fun `exact match with high confidence scores higher than a poor match`() = runBlocking {
        val reference = "I want a coffee please"

        val goodAttempt = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor("I want a coffee please", confidence = 0.95f),
            nativeLanguageTag = "en",
        )

        val poorAttempt = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor("uh want uh coffee", confidence = 0.3f),
            nativeLanguageTag = "en",
        )

        assertTrue(
            "Good attempt (${goodAttempt.accuracy}) should score higher than poor attempt (${poorAttempt.accuracy})",
            goodAttempt.accuracy > poorAttempt.accuracy,
        )
        assertTrue(goodAttempt.overall > poorAttempt.overall)
    }

    @Test
    fun `higher per-word confidence increases accuracy for the same word match`() = runBlocking {
        val reference = "she likes red apples"

        val highConfidence = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor(reference, confidence = 0.9f),
            nativeLanguageTag = "en",
        )
        val lowConfidence = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor(reference, confidence = 0.4f),
            nativeLanguageTag = "en",
        )

        assertTrue(highConfidence.accuracy > lowConfidence.accuracy)
    }

    @Test
    fun `completely unrelated speech scores near zero accuracy`() = runBlocking {
        val result = scorer.score(
            referenceText = "the weather is nice today",
            recognizedWords = wordsFor("banana", confidence = 0.9f),
            nativeLanguageTag = "en",
        )
        assertTrue("Expected low accuracy, got ${result.accuracy}", result.accuracy < 30f)
    }

    @Test
    fun `evenly paced words score higher rhythm than very uneven pacing`() = runBlocking {
        val evenWords = listOf(
            RecognizedWord("one", 0, 300, confidence = 0.9f),
            RecognizedWord("two", 300, 600, confidence = 0.9f),
            RecognizedWord("three", 600, 900, confidence = 0.9f),
        )
        val unevenWords = listOf(
            RecognizedWord("one", 0, 50, confidence = 0.9f),
            RecognizedWord("two", 50, 2000, confidence = 0.9f),
            RecognizedWord("three", 2000, 2080, confidence = 0.9f),
        )

        val even = scorer.score("one two three", evenWords, "en")
        val uneven = scorer.score("one two three", unevenWords, "en")

        assertTrue(
            "Even pacing (${even.rhythm}) should score higher rhythm than uneven pacing (${uneven.rhythm})",
            even.rhythm > uneven.rhythm,
        )
    }

    @Test
    fun `problem sound score reflects confidence of words that actually exercise that sound`() = runBlocking {
        // Korean speakers' profile includes L_R - "light" and "right" both exercise it.
        val reference = "turn on the light and read the letter"

        val clearLr = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor(reference, confidence = 0.95f),
            nativeLanguageTag = "ko",
        )
        val unclearLr = scorer.score(
            referenceText = reference,
            recognizedWords = wordsFor("turn on the light and read the letter", confidence = 0.2f),
            nativeLanguageTag = "ko",
        )

        val clearScore = clearLr.problemSoundScores.getValue(ProblemSound.L_R.name)
        val unclearScore = unclearLr.problemSoundScores.getValue(ProblemSound.L_R.name)
        assertTrue(
            "Clearly-spoken L/R words ($clearScore) should score higher than unclear ones ($unclearScore)",
            clearScore > unclearScore,
        )
    }

    /** Builds evenly-timed [RecognizedWord]s for a space-separated string, all at [confidence]. */
    private fun wordsFor(text: String, confidence: Float): List<RecognizedWord> {
        val words = text.split(" ").filter { it.isNotBlank() }
        return words.mapIndexed { index, word ->
            val start = index * 400L
            RecognizedWord(word = word, startMs = start, endMs = start + 350L, confidence = confidence)
        }
    }
}
