package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.englishcoach.app.core.database.entity.LessonAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonAttemptDao {
    @Query("SELECT * FROM lesson_attempts WHERE lessonId = :lessonId ORDER BY startedAtEpochMs DESC")
    fun observeForLesson(lessonId: String): Flow<List<LessonAttemptEntity>>

    @Query("SELECT * FROM lesson_attempts WHERE lessonId = :lessonId ORDER BY startedAtEpochMs DESC LIMIT 1")
    suspend fun lastForLesson(lessonId: String): LessonAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: LessonAttemptEntity)
}
