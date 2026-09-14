package com.example.bluff.domain.model

import java.time.LocalDate

/**
 * Domain model for a savings goal.
 */
data class Goal(
    val id: String,
    val userId: String,
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long = 0L,
    val targetDate: LocalDate? = null,
    val icon: String = "savings",
    val color: String = "#6C63FF",
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Float get() = if (targetAmountMinor > 0) (currentAmountMinor.toFloat() / targetAmountMinor.toFloat()) * 100f else 0f
    val remainingMinor: Long get() = (targetAmountMinor - currentAmountMinor).coerceAtLeast(0L)
}
