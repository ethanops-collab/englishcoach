package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.englishcoach.app.core.database.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = ${UserProgressEntity.SINGLETON_ID}")
    fun observe(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = ${UserProgressEntity.SINGLETON_ID}")
    suspend fun get(): UserProgressEntity?

    @Upsert
    suspend fun upsert(progress: UserProgressEntity)
}
