package com.example.bluff.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BudgetTest {

    @Test
    fun `remainingMinor calculates correctly`() {
        val budget = Budget(
            id = "1", userId = "", name = "Food",
            amountMinor = 10000L, spentMinor = 3000L,
            startDate = LocalDate.now()
        )
        assertEquals(7000L, budget.remainingMinor)
    }

    @Test
    fun `percentUsed calculates correctly`() {
        val budget = Budget(
            id = "1", userId = "", name = "Food",
            amountMinor = 10000L, spentMinor = 2500L,
            startDate = LocalDate.now()
        )
        assertEquals(25f, budget.percentUsed, 0.01f)
    }

    @Test
    fun `isOverBudget when spent exceeds amount`() {
        val budget = Budget(
            id = "1", userId = "", name = "Food",
            amountMinor = 5000L, spentMinor = 6000L,
            startDate = LocalDate.now()
        )
        assertTrue(budget.isOverBudget)
    }

    @Test
    fun `isOverBudget false when within budget`() {
        val budget = Budget(
            id = "1", userId = "", name = "Food",
            amountMinor = 10000L, spentMinor = 5000L,
            startDate = LocalDate.now()
        )
        assertFalse(budget.isOverBudget)
    }

    @Test
    fun `percentUsed handles zero amount safely`() {
        val budget = Budget(
            id = "1", userId = "", name = "Food",
            amountMinor = 0L, spentMinor = 0L,
            startDate = LocalDate.now()
        )
        assertEquals(0f, budget.percentUsed, 0.01f)
    }
}
