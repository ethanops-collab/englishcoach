package com.englishcoach.app.core.model

data class LessonAttempt(
    val id: String,
    val lessonId: String,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val turns: List<ConversationTurn>,
    val fluencyScore: Float?,
    val pronunciationScore: PronunciationScore?,
    val xpEarned: Int,
    val wordsToReview: List<String>,
)
