package com.example.bluff.domain.model

import java.time.LocalDate

/**
 * Domain model for a financial account.
 * All monetary values stored as Long (minor units = paise for INR).
 */
data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: AccountType,
    val currencyCode: String = "INR",
    val initialBalanceMinor: Long = 0L,
    val icon: String = "wallet",
    val color: String = "#6C63FF",
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Computed - populated by repository
    val currentBalanceMinor: Long = initialBalanceMinor
)
