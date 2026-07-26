package com.englishcoach.app.core.database.repository

import com.englishcoach.app.core.database.dao.MistakeDao
import com.englishcoach.app.core.database.mapper.toDomain
import com.englishcoach.app.core.database.mapper.toEntity
import com.englishcoach.app.core.model.MistakeRecord
import com.englishcoach.app.domain.repository.MistakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomMistakeRepository @Inject constructor(
    private val mistakeDao: MistakeDao,
) : MistakeRepository {

    override suspend fun recordMistake(mistake: MistakeRecord) {
        mistakeDao.insert(mistake.toEntity())
    }

    override fun observeDueMistakes(nowEpochMs: Long): Flow<List<MistakeRecord>> =
        mistakeDao.observeDue(nowEpochMs).map { entities -> entities.map { it.toDomain() } }

    override fun observeRecentMistakes(lessonId: String): Flow<List<MistakeRecord>> =
        mistakeDao.observeForLesson(lessonId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun markReviewed(mistakeId: String, nextReviewAtEpochMs: Long, newIntervalDays: Int, timesReviewed: Int) {
        mistakeDao.markReviewed(mistakeId, nextReviewAtEpochMs, newIntervalDays, timesReviewed)
    }
}
