package com.englishcoach.app.core.database.repository

import com.englishcoach.app.core.database.dao.ConversationTurnDao
import com.englishcoach.app.core.database.dao.LessonAttemptDao
import com.englishcoach.app.core.database.dao.LessonDao
import com.englishcoach.app.core.database.mapper.toDomain
import com.englishcoach.app.core.database.mapper.toEntity
import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomLessonRepository @Inject constructor(
    private val lessonDao: LessonDao,
    private val lessonAttemptDao: LessonAttemptDao,
    private val conversationTurnDao: ConversationTurnDao,
) : LessonRepository {

    override fun observeLessons(): Flow<List<Lesson>> =
        lessonDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLesson(id: String): Lesson? = lessonDao.getById(id)?.toDomain()

    override suspend fun ensureSeeded(lessons: List<Lesson>) {
        if (lessonDao.count() == 0) {
            lessonDao.insertAll(lessons.map { it.toEntity() })
        }
    }

    override suspend fun saveAttempt(attempt: LessonAttempt) {
        lessonAttemptDao.insert(attempt.toEntity())
        conversationTurnDao.insertAll(attempt.turns.map { it.toEntity(attempt.id) })
    }

    override fun observeAttempts(lessonId: String): Flow<List<LessonAttempt>> =
        lessonAttemptDao.observeForLesson(lessonId).map { entities ->
            entities.map { entity -> entity.toDomain(conversationTurnDao.getForAttempt(entity.id).map { it.toDomain() }) }
        }

    override suspend fun lastAttempt(lessonId: String): LessonAttempt? {
        val entity = lessonAttemptDao.lastForLesson(lessonId) ?: return null
        return entity.toDomain(conversationTurnDao.getForAttempt(entity.id).map { it.toDomain() })
    }
}
