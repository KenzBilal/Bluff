package com.example.bluff.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class GoalTest {

    @Test
    fun `progressPercent calculates correctly`() {
        val goal = Goal(
            id = "1", userId = "", name = "MacBook",
            targetAmountMinor = 80000L, currentAmountMinor = 20000L,
            targetDate = LocalDate.now().plusMonths(6)
        )
        assertEquals(25f, goal.progressPercent, 0.01f)
    }

    @Test
    fun `remainingMinor calculates correctly`() {
        val goal = Goal(
            id = "1", userId = "", name = "MacBook",
            targetAmountMinor = 80000L, currentAmountMinor = 21500L
        )
        assertEquals(58500L, goal.remainingMinor)
    }

    @Test
    fun `remainingMinor clamps to zero when overfunded`() {
        val goal = Goal(
            id = "1", userId = "", name = "MacBook",
            targetAmountMinor = 80000L, currentAmountMinor = 90000L
        )
        assertEquals(0L, goal.remainingMinor)
    }

    @Test
    fun `progressPercent handles zero target safely`() {
        val goal = Goal(
            id = "1", userId = "", name = "Empty",
            targetAmountMinor = 0L, currentAmountMinor = 0L
        )
        assertEquals(0f, goal.progressPercent, 0.01f)
    }
}
