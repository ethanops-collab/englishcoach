package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mistakes", indices = [Index("lessonId"), Index("nextReviewAtEpochMs")])
data class MistakeRecordEntity(
    @PrimaryKey val id: String,
    val type: String,
    val lessonId: String,
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val createdAtEpochMs: Long,
    val nextReviewAtEpochMs: Long,
    val intervalDays: Int,
    val timesReviewed: Int,
)
