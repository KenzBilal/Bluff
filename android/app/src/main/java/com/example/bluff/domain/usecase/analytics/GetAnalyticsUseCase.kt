package com.example.bluff.domain.usecase.analytics

import com.example.bluff.data.repository.TransactionRepository
import com.example.bluff.domain.model.AnalyticsSummary
import com.example.bluff.domain.model.CategorySpending
import com.example.bluff.domain.model.DailySpending
import com.example.bluff.domain.model.Insight
import com.example.bluff.domain.model.InsightType
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GetAnalyticsUseCase(private val transactionRepository: TransactionRepository) {

    fun getForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<AnalyticsSummary> {
        return transactionRepository.getTransactionsByDateRange(startDate, endDate)
            .map { transactions -> buildSummary(transactions, startDate, endDate) }
    }

    private fun buildSummary(
        transactions: List<Transaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): AnalyticsSummary {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val incomes = transactions.filter { it.type == TransactionType.INCOME }

        val totalSpent = expenses.sumOf { it.amountMinor }
        val totalIncome = incomes.sumOf { it.amountMinor }

        val dayCount = (endDate.toEpochDay() - startDate.toEpochDay() + 1).coerceAtLeast(1)
        val avgDaily = totalSpent / dayCount

        val largest = expenses.maxByOrNull { it.amountMinor }

        // Category breakdown
        val categoryMap = expenses.groupBy { it.categoryId ?: "uncategorized" }
        val categoryBreakdown = categoryMap.map { (_, txns) ->
            val amount = txns.sumOf { it.amountMinor }
            CategorySpending(
                categoryId = txns.firstOrNull()?.categoryId ?: "uncategorized",
                categoryName = txns.firstOrNull()?.categoryName ?: "Other",
                categoryIcon = txns.firstOrNull()?.categoryIcon ?: "📦",
                categoryColor = txns.firstOrNull()?.categoryColor ?: "#888888",
                amountMinor = amount,
                percentage = if (totalSpent > 0) (amount.toFloat() / totalSpent.toFloat()) * 100f else 0f
            )
        }.sortedByDescending { it.amountMinor }

        // Daily spending
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dailyMap = expenses.groupBy { it.transactionDate.format(fmt) }
        val dailySpending = dailyMap.map { (date, txns) ->
            DailySpending(date = date, amountMinor = txns.sumOf { it.amountMinor })
        }.sortedBy { it.date }

        val insights = buildInsights(
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            categoryBreakdown = categoryBreakdown,
            endDate = endDate
        )

        return AnalyticsSummary(
            totalSpentMinor = totalSpent,
            totalIncomeMinor = totalIncome,
            averageDailySpendingMinor = avgDaily,
            largestExpenseMinor = largest?.amountMinor ?: 0L,
            largestExpenseName = largest?.categoryName ?: "",
            categoryBreakdown = categoryBreakdown,
            dailySpending = dailySpending,
            insights = insights
        )
    }

    private fun buildInsights(
        totalSpent: Long,
        totalIncome: Long,
        categoryBreakdown: List<CategorySpending>,
        endDate: LocalDate
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        categoryBreakdown.firstOrNull()?.let { top ->
            insights.add(Insight(
                icon = top.categoryIcon,
                message = "${top.categoryName} is your biggest expense (${top.percentage.toInt()}% of spending)",
                type = InsightType.INFO
            ))
        }

        if (totalIncome > 0) {
            val savings = totalIncome - totalSpent
            val rate = (savings.toFloat() / totalIncome.toFloat() * 100).toInt()
            when {
                rate >= 20 -> insights.add(Insight("💚", "Great job! You saved $rate% of your income", InsightType.POSITIVE))
                rate > 0 -> insights.add(Insight("💰", "You saved $rate% of your income this period", InsightType.INFO))
                else -> insights.add(Insight("⚠️", "You spent more than you earned this period", InsightType.WARNING))
            }
        }

        val today = LocalDate.now()
        if (endDate.isAfter(today)) {
            val daysRemaining = endDate.toEpochDay() - today.toEpochDay()
            insights.add(Insight("📅", "$daysRemaining days remaining in this period", InsightType.INFO))
        }

        return insights
    }
}
