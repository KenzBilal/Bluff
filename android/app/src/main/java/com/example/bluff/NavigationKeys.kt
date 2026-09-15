package com.example.bluff

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeKey : NavKey
@Serializable data object CalendarKey : NavKey
@Serializable data object TransactionsKey : NavKey
@Serializable data object AnalyticsKey : NavKey
@Serializable data object MoreKey : NavKey
@Serializable data object AccountsKey : NavKey
@Serializable data object BudgetsKey : NavKey
@Serializable data object GoalsKey : NavKey
@Serializable data object CategoriesKey : NavKey
@Serializable data object RecurringKey : NavKey
@Serializable data object SettingsKey : NavKey
@Serializable data class TransactionDetailKey(val transactionId: String) : NavKey
@Serializable data class DayDetailKey(val date: String) : NavKey
@Serializable data object OnboardingKey : NavKey
