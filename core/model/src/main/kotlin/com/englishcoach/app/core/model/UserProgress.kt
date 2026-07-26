package com.englishcoach.app.core.model

data class UserProgress(
    val streakDays: Int,
    val xp: Int,
    val level: Int,
    val totalSpeakingMinutes: Int,
    val lastActiveEpochDay: Long,
) {
    companion object {
        fun initial() = UserProgress(
            streakDays = 0,
            xp = 0,
            level = 1,
            totalSpeakingMinutes = 0,
            lastActiveEpochDay = 0L,
        )
    }
}
