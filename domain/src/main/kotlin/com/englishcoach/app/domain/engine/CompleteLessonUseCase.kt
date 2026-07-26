package com.englishcoach.app.domain.engine

import com.englishcoach.app.core.model.ConversationTurn
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.core.model.Speaker
import com.englishcoach.app.domain.repository.LessonRepository
import com.englishcoach.app.domain.repository.ProgressRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Runs at the end of a lesson: computes fluency/XP, persists the attempt, and rolls the
 * result into streak/level - everything the "After Every Lesson" summary screen needs.
 */
class CompleteLessonUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(session: SessionUiState, speakingSeconds: Int): LessonAttempt {
        val lesson = requireNotNull(session.lesson) { "Cannot complete a session with no lesson" }
        val mistakesFixed = session.turns.count { it.correction != null }
        val pronunciationOverall = session.lastPronunciationScore?.overall ?: 0f
        val fluencyScore = computeFluency(session.turns)
        val xpEarned = GamificationCalculator.xpForSession(pronunciationOverall, fluencyScore, mistakesFixed)

        val attempt = LessonAttempt(
            id = UUID.randomUUID().toString(),
            lessonId = lesson.id,
            startedAtEpochMs = session.turns.firstOrNull()?.timestampMs ?: System.currentTimeMillis(),
            completedAtEpochMs = System.currentTimeMillis(),
            turns = session.turns,
            fluencyScore = fluencyScore,
            pronunciationScore = session.lastPronunciationScore,
            xpEarned = xpEarned,
            wordsToReview = session.turns.mapNotNull { it.correction?.correctedText },
        )

        lessonRepository.saveAttempt(attempt)
        progressRepository.applySessionResult(
            xpEarned = xpEarned,
            speakingMinutes = (speakingSeconds / 60).coerceAtLeast(1),
            completedAtEpochDay = System.currentTimeMillis() / MS_PER_DAY,
        )
        return attempt
    }

    private fun computeFluency(turns: List<ConversationTurn>): Float {
        val userTurnCount = turns.count { it.speaker == Speaker.USER }
        if (userTurnCount == 0) return 0f
        val mistakeCount = turns.count { it.correction != null }
        val accuracyRatio = 1f - (mistakeCount.toFloat() / userTurnCount.toFloat())
        return (accuracyRatio * 100f).coerceIn(0f, 100f)
    }

    private companion object {
        const val MS_PER_DAY = 86_400_000L
    }
}
