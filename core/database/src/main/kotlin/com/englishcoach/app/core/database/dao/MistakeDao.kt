package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.englishcoach.app.core.database.entity.MistakeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE nextReviewAtEpochMs <= :nowEpochMs ORDER BY nextReviewAtEpochMs ASC")
    fun observeDue(nowEpochMs: Long): Flow<List<MistakeRecordEntity>>

    @Query("SELECT * FROM mistakes WHERE lessonId = :lessonId ORDER BY createdAtEpochMs DESC")
    fun observeForLesson(lessonId: String): Flow<List<MistakeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mistake: MistakeRecordEntity)

    @Query(
        "UPDATE mistakes SET nextReviewAtEpochMs = :nextReviewAtEpochMs, intervalDays = :intervalDays, " +
            "timesReviewed = :timesReviewed WHERE id = :id",
    )
    suspend fun markReviewed(id: String, nextReviewAtEpochMs: Long, intervalDays: Int, timesReviewed: Int)
}
