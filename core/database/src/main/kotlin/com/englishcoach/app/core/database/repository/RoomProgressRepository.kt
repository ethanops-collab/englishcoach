package com.englishcoach.app.core.database.repository

import com.englishcoach.app.core.database.dao.UserProgressDao
import com.englishcoach.app.core.database.entity.UserProgressEntity
import com.englishcoach.app.core.database.mapper.toDomain
import com.englishcoach.app.core.model.UserProgress
import com.englishcoach.app.domain.engine.GamificationCalculator
import com.englishcoach.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomProgressRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
) : ProgressRepository {

    override fun observeProgress(): Flow<UserProgress> =
        userProgressDao.observe().map { it?.toDomain() ?: UserProgress.initial() }

    override suspend fun applySessionResult(xpEarned: Int, speakingMinutes: Int, completedAtEpochDay: Long) {
        val current = userProgressDao.get() ?: UserProgressEntity.initial()
        val newXp = current.xp + xpEarned
        val updated = current.copy(
            xp = newXp,
            level = GamificationCalculator.levelForXp(newXp),
            totalSpeakingMinutes = current.totalSpeakingMinutes + speakingMinutes,
            streakDays = GamificationCalculator.updateStreak(
                lastActiveEpochDay = current.lastActiveEpochDay,
                todayEpochDay = completedAtEpochDay,
                currentStreak = current.streakDays,
            ),
            lastActiveEpochDay = completedAtEpochDay,
        )
        userProgressDao.upsert(updated)
    }
}
