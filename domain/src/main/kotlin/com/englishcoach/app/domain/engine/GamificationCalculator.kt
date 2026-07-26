package com.englishcoach.app.domain.engine

/** Pure functions, deliberately stateless so they're trivial to unit test. */
object GamificationCalculator {
    private const val XP_PER_LEVEL = 500
    private const val BASE_SESSION_XP = 40
    private const val XP_PER_FIXED_MISTAKE = 5

    fun xpForSession(pronunciationOverall: Float, fluencyScore: Float, mistakesFixed: Int): Int {
        val qualityBonus = (((pronunciationOverall + fluencyScore) / 2f) / 10f).toInt()
        return BASE_SESSION_XP + qualityBonus + mistakesFixed * XP_PER_FIXED_MISTAKE
    }

    fun levelForXp(totalXp: Int): Int = totalXp / XP_PER_LEVEL + 1

    /**
     * [lastActiveEpochDay]/[todayEpochDay] are days since epoch (e.g. `epochMs / 86_400_000`).
     * Practicing again the same day holds the streak, practicing exactly the next day extends
     * it, any bigger gap resets it to 1.
     */
    fun updateStreak(lastActiveEpochDay: Long, todayEpochDay: Long, currentStreak: Int): Int = when {
        lastActiveEpochDay == todayEpochDay -> currentStreak.coerceAtLeast(1)
        lastActiveEpochDay == todayEpochDay - 1 -> currentStreak + 1
        else -> 1
    }
}
