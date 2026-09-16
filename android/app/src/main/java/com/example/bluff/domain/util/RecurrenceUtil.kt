package com.example.bluff.domain.util

import com.example.bluff.domain.model.RecurrenceFrequency
import java.time.LocalDate

object RecurrenceUtil {
    fun calculateNextRunDate(current: LocalDate, frequency: RecurrenceFrequency): LocalDate {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> current.plusDays(1)
            RecurrenceFrequency.WEEKLY -> current.plusWeeks(1)
            RecurrenceFrequency.MONTHLY -> current.plusMonths(1)
            RecurrenceFrequency.YEARLY -> current.plusYears(1)
        }
    }

    fun firstOccurrenceAfter(startDate: LocalDate, frequency: RecurrenceFrequency, after: LocalDate): LocalDate {
        var date = startDate
        while (!date.isAfter(after)) {
            date = calculateNextRunDate(date, frequency)
        }
        return date
    }
}
