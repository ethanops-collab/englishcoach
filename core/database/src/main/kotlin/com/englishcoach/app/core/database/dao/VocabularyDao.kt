package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.englishcoach.app.core.database.entity.VocabularyItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY learnedAtEpochMs DESC")
    fun observeAll(): Flow<List<VocabularyItemEntity>>

    @Query("SELECT * FROM vocabulary WHERE lessonId = :lessonId ORDER BY learnedAtEpochMs DESC")
    fun observeForLesson(lessonId: String): Flow<List<VocabularyItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VocabularyItemEntity)
}
