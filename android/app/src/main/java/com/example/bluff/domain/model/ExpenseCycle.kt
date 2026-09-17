package com.example.bluff.domain.model

import java.time.LocalDate

data class ExpenseCycle(
    val id: String,
    val categoryId: String,
    val name: String,
    val amountMinor: Long,
    val cycleDays: Int,
    val lastTransactionDate: LocalDate,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
) {
    val daysUntilDue: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextDueDate)

    val isOverdue: Boolean
        get() = daysUntilDue < 0

    val isDueSoon: Boolean
        get() = daysUntilDue in 0..2

    val statusText: String
        get() = when {
            isOverdue -> "overdue by ${-daysUntilDue} days"
            daysUntilDue == 0L -> "due today"
            daysUntilDue == 1L -> "due tomorrow"
            else -> "in $daysUntilDue days"
        }
}

data class CategoryCycleDefault(
    val categoryId: String,
    val categoryName: String,
    val defaultCycleDays: Int
)
