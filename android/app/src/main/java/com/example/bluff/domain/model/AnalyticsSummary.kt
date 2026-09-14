package com.example.bluff.domain.model

/**
 * Analytics summary for a given time period.
 * All monetary values in minor units (paise).
 */
data class AnalyticsSummary(
    val totalSpentMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val averageDailySpendingMinor: Long = 0L,
    val largestExpenseMinor: Long = 0L,
    val largestExpenseName: String = "",
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val insights: List<Insight> = emptyList()
) {
    val totalSavingsMinor: Long get() = totalIncomeMinor - totalSpentMinor
    val savingsRate: Float get() = if (totalIncomeMinor > 0) (totalSavingsMinor.toFloat() / totalIncomeMinor.toFloat()) * 100f else 0f
}

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val amountMinor: Long,
    val percentage: Float
)

data class DailySpending(
    val date: String, // "yyyy-MM-dd"
    val amountMinor: Long
)

data class Insight(
    val icon: String,
    val message: String,
    val type: InsightType = InsightType.INFO
)

enum class InsightType { INFO, WARNING, POSITIVE }
