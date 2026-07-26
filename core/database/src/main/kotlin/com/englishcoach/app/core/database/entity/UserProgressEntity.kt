package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton row: [id] is always [SINGLETON_ID]. */
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val streakDays: Int,
    val xp: Int,
    val level: Int,
    val totalSpeakingMinutes: Int,
    val lastActiveEpochDay: Long,
) {
    companion object {
        const val SINGLETON_ID = 0

        fun initial() = UserProgressEntity(
            streakDays = 0,
            xp = 0,
            level = 1,
            totalSpeakingMinutes = 0,
            lastActiveEpochDay = 0L,
        )
    }
}
