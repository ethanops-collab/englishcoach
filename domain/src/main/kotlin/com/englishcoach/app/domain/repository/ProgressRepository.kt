package com.englishcoach.app.domain.repository

import com.englishcoach.app.core.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeProgress(): Flow<UserProgress>

    /** Applies one completed session's results, recalculating streak/level as needed. */
    suspend fun applySessionResult(
        xpEarned: Int,
        speakingMinutes: Int,
        completedAtEpochDay: Long,
    )
}
