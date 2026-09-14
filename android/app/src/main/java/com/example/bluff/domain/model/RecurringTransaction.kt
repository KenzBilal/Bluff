package com.example.bluff.domain.model

import java.time.LocalDate

data class RecurringTransaction(
    val id: String,
    val userId: String,
    val name: String,
    val amountMinor: Long,
    val type: TransactionType,
    val accountId: String,
    val categoryId: String? = null,
    val frequency: RecurrenceFrequency,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val nextRunDate: LocalDate,
    val isActive: Boolean = true,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Denormalized for display
    val accountName: String = "",
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null
)
