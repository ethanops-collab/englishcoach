package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_attempts", indices = [Index("lessonId")])
data class LessonAttemptEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val startedAtEpochMs: Long,
    val completedAtEpochMs: Long?,
    val fluencyScore: Float?,
    val pronunciationOverall: Float?,
    val pronunciationAccuracy: Float?,
    val pronunciationStress: Float?,
    val pronunciationRhythm: Float?,
    val pronunciationIntonation: Float?,
    val pronunciationMissingSoundsCsv: String?,
    val pronunciationProblemSoundScoresJson: String?,
    val xpEarned: Int,
    val wordsToReviewCsv: String,
)
