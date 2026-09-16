package com.example.bluff.domain.model

import java.time.LocalDate

/**
 * Domain model for a financial transaction.
 * amountMinor is always positive (paise for INR).
 * Type determines whether it's a debit/credit/transfer.
 */
data class Transaction(
    val id: String,
    val userId: String,
    val amountMinor: Long,
    val currencyCode: String = "INR",
    val type: TransactionType,
    val accountId: String,
    val toAccountId: String? = null,
    val categoryId: String? = null,
    val note: String? = null,
    val transactionDate: LocalDate,
    val recurringId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val goalId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    // Denormalized for display (populated by repository joins)
    val accountName: String = "",
    val toAccountName: String? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val tags: List<String> = emptyList()
)
