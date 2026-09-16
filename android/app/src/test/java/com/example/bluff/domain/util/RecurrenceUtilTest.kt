package com.example.bluff.domain.util

import com.example.bluff.domain.model.RecurrenceFrequency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RecurrenceUtilTest {
    @Test
    fun `daily advances by 1 day`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2026, 9, 18), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.DAILY))
    }

    @Test
    fun `weekly advances by 7 days`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2026, 9, 24), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.WEEKLY))
    }

    @Test
    fun `monthly advances by 1 month`() {
        val date = LocalDate.of(2026, 1, 15)
        assertEquals(LocalDate.of(2026, 2, 15), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.MONTHLY))
    }

    @Test
    fun `yearly advances by 1 year`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2027, 9, 17), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.YEARLY))
    }

    @Test
    fun `firstOccurrenceAfter skips past dates`() {
        val start = LocalDate.of(2026, 1, 1)
        val after = LocalDate.of(2026, 9, 17)
        val result = RecurrenceUtil.firstOccurrenceAfter(start, RecurrenceFrequency.MONTHLY, after)
        assertEquals(LocalDate.of(2026, 10, 1), result)
    }
}
