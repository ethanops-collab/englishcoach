package com.englishcoach.app.domain.engine

/** Simple fixed-step spaced-repetition schedule (1 -> 3 -> 7 -> 14 -> 30 days). */
object ReviewScheduler {
    private val stepsDays = intArrayOf(1, 3, 7, 14, 30)

    fun nextIntervalDays(currentIntervalDays: Int): Int {
        val index = stepsDays.indexOf(currentIntervalDays)
        return if (index == -1 || index == stepsDays.lastIndex) stepsDays.last() else stepsDays[index + 1]
    }

    fun nextReviewAtEpochMs(nowEpochMs: Long, intervalDays: Int): Long =
        nowEpochMs + intervalDays * 24L * 60L * 60L * 1000L
}
