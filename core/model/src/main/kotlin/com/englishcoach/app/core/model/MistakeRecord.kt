package com.englishcoach.app.core.model

enum class MistakeType { GRAMMAR, PRONUNCIATION, VOCABULARY }

/**
 * A single missed item queued for spaced review. [nextReviewAtEpochMs] and [intervalDays]
 * are updated by the review scheduler in :domain each time the user re-attempts the item.
 */
data class MistakeRecord(
    val id: String,
    val type: MistakeType,
    val lessonId: String,
    val originalText: String,
    val correctedText: String,
    val explanation: String,
    val createdAtEpochMs: Long,
    val nextReviewAtEpochMs: Long,
    val intervalDays: Int,
    val timesReviewed: Int,
)
