package com.englishcoach.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.englishcoach.app.core.database.entity.ConversationTurnEntity

@Dao
interface ConversationTurnDao {
    @Query("SELECT * FROM conversation_turns WHERE attemptId = :attemptId ORDER BY timestampMs ASC")
    suspend fun getForAttempt(attemptId: String): List<ConversationTurnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turns: List<ConversationTurnEntity>)
}
