package com.englishcoach.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationCalculatorTest {

    @Test
    fun `streak extends on the very next day`() {
        val streak = GamificationCalculator.updateStreak(lastActiveEpochDay = 10, todayEpochDay = 11, currentStreak = 4)
        assertEquals(5, streak)
    }

    @Test
    fun `streak resets after a gap`() {
        val streak = GamificationCalculator.updateStreak(lastActiveEpochDay = 10, todayEpochDay = 13, currentStreak = 4)
        assertEquals(1, streak)
    }

    @Test
    fun `streak holds when practicing again the same day`() {
        val streak = GamificationCalculator.updateStreak(lastActiveEpochDay = 10, todayEpochDay = 10, currentStreak = 4)
        assertEquals(4, streak)
    }

    @Test
    fun `level increases every 500 xp`() {
        assertEquals(1, GamificationCalculator.levelForXp(0))
        assertEquals(2, GamificationCalculator.levelForXp(500))
        assertEquals(3, GamificationCalculator.levelForXp(1000))
    }
}
