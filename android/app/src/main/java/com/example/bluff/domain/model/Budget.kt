package com.example.bluff.domain.model

import java.time.LocalDate

/**
 * Domain model for a spending budget.
 * categoryId = null means it's an overall/total budget.
 */
data class Budget(
    val id: String,
    val userId: String,
    val name: String,
    val amountMinor: Long,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val categoryId: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Computed - populated by repository
    val spentMinor: Long = 0L,
    val categoryName: String? = null,
    val categoryColor: String? = null,
    val categoryIcon: String? = null
) {
    val remainingMinor: Long get() = amountMinor - spentMinor
    val percentUsed: Float get() = if (amountMinor > 0) (spentMinor.toFloat() / amountMinor.toFloat()) * 100f else 0f
    val isOverBudget: Boolean get() = spentMinor > amountMinor
}
