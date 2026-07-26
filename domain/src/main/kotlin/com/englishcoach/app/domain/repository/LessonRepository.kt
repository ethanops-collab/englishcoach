package com.englishcoach.app.domain.repository

import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.LessonAttempt
import kotlinx.coroutines.flow.Flow

/** Implemented in :core:database. The seed catalog itself lives in [com.englishcoach.app.domain.content.LessonCatalog]. */
interface LessonRepository {
    fun observeLessons(): Flow<List<Lesson>>
    suspend fun getLesson(id: String): Lesson?
    suspend fun ensureSeeded(lessons: List<Lesson>)

    suspend fun saveAttempt(attempt: LessonAttempt)
    fun observeAttempts(lessonId: String): Flow<List<LessonAttempt>>
    suspend fun lastAttempt(lessonId: String): LessonAttempt?
}
