package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.englishcoach.app.core.database.entity.CharacterPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterPreferenceDao {
    @Query("SELECT * FROM character_preferences WHERE lessonType = :lessonType")
    fun observe(lessonType: String): Flow<CharacterPreferenceEntity?>

    @Upsert
    suspend fun upsert(entity: CharacterPreferenceEntity)

    @Query("DELETE FROM character_preferences WHERE lessonType = :lessonType")
    suspend fun clear(lessonType: String)
}
