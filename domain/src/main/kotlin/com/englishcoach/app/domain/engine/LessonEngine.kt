package com.englishcoach.app.domain.engine

import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.domain.content.LessonCatalog
import com.englishcoach.app.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Picks "Today's Mission" and exposes the lesson catalog - the anti-blank-screen entry point. */
class LessonEngine @Inject constructor(
    private val lessonRepository: LessonRepository,
) {
    suspend fun ensureCatalogSeeded() {
        lessonRepository.ensureSeeded(LessonCatalog.all)
    }

    fun observeLessons(): Flow<List<Lesson>> = lessonRepository.observeLessons()

    /** The least-recently-practiced lesson, or the first lesson if nothing's been attempted. */
    suspend fun todaysMission(): Lesson {
        val lessons = lessonRepository.observeLessons().first().ifEmpty { LessonCatalog.all }
        return lessons.minByOrNull { lesson ->
            lessonRepository.lastAttempt(lesson.id)?.completedAtEpochMs ?: 0L
        } ?: LessonCatalog.all.first()
    }
}
