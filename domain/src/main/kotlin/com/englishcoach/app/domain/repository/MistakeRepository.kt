package com.englishcoach.app.domain.repository

import com.englishcoach.app.core.model.MistakeRecord
import kotlinx.coroutines.flow.Flow

interface MistakeRepository {
    suspend fun recordMistake(mistake: MistakeRecord)
    fun observeDueMistakes(nowEpochMs: Long): Flow<List<MistakeRecord>>
    fun observeRecentMistakes(lessonId: String): Flow<List<MistakeRecord>>
    suspend fun markReviewed(mistakeId: String, nextReviewAtEpochMs: Long, newIntervalDays: Int, timesReviewed: Int)
}
