package com.example.bluff.di

import android.content.Context
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.preferences.UserPreferencesManager
import com.example.bluff.data.repository.AccountRepositoryImpl
import com.example.bluff.data.repository.AppSettingsRepositoryImpl
import com.example.bluff.data.repository.BudgetRepositoryImpl
import com.example.bluff.data.repository.CategoryRepositoryImpl
import com.example.bluff.data.repository.DebtRepositoryImpl
import com.example.bluff.data.repository.ExpenseCycleRepositoryImpl
import com.example.bluff.data.repository.GoalRepositoryImpl
import com.example.bluff.data.repository.RecurringTransactionRepositoryImpl
import com.example.bluff.data.repository.TransactionRepositoryImpl
import com.example.bluff.domain.repository.ExpenseCycleRepository
import com.example.bluff.domain.usecase.account.AddAccountUseCase
import com.example.bluff.domain.usecase.account.DeleteAccountUseCase
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.account.UpdateAccountUseCase
import com.example.bluff.domain.usecase.analytics.GetAnalyticsUseCase
import com.example.bluff.domain.usecase.debt.AddDebtUseCase
import com.example.bluff.domain.usecase.debt.DeleteDebtUseCase
import com.example.bluff.domain.usecase.debt.GetDebtsUseCase
import com.example.bluff.domain.usecase.debt.MarkDebtPaidUseCase
import com.example.bluff.domain.usecase.budget.DeleteBudgetUseCase
import com.example.bluff.domain.usecase.budget.GetBudgetsUseCase
import com.example.bluff.domain.usecase.budget.UpsertBudgetUseCase
import com.example.bluff.domain.usecase.category.AddCategoryUseCase
import com.example.bluff.domain.usecase.category.DeleteCategoryUseCase
import com.example.bluff.domain.usecase.category.GetCategoriesUseCase
import com.example.bluff.domain.usecase.cycle.AddCycleUseCase
import com.example.bluff.domain.usecase.cycle.DeactivateCycleUseCase
import com.example.bluff.domain.usecase.cycle.GetCyclesUseCase
import com.example.bluff.domain.usecase.cycle.MarkCyclePaidUseCase
import com.example.bluff.domain.usecase.goal.DeleteGoalUseCase
import com.example.bluff.domain.usecase.goal.GetGoalsUseCase
import com.example.bluff.domain.usecase.goal.UpsertGoalUseCase
import com.example.bluff.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.example.bluff.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.example.bluff.domain.usecase.recurring.UpsertRecurringTransactionUseCase
import com.example.bluff.domain.usecase.settings.GetAppSettingsUseCase
import com.example.bluff.domain.usecase.settings.UpdateAppSettingsUseCase
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import com.example.bluff.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.bluff.domain.usecase.transaction.GetTransactionsUseCase
import com.example.bluff.domain.usecase.transaction.UpdateTransactionUseCase

/**
 * Manual dependency injection container.
 * Initialized once in BluffApplication and accessed as a singleton.
 */
class AppContainer(context: Context) {

    // --- Infrastructure ---
    private val database = BluffDatabase.create(context)
    val userPreferencesManager = UserPreferencesManager(context)
    private fun getUserId() = userPreferencesManager.getUserIdBlocking()

    // --- Repositories ---
    val accountRepository = AccountRepositoryImpl(database) { getUserId() }
    val categoryRepository = CategoryRepositoryImpl(database) { getUserId() }
    val transactionRepository = TransactionRepositoryImpl(database) { getUserId() }
    val budgetRepository = BudgetRepositoryImpl(database) { getUserId() }
    val goalRepository = GoalRepositoryImpl(database) { getUserId() }
    val recurringTransactionRepository = RecurringTransactionRepositoryImpl(database) { getUserId() }
    val appSettingsRepository = AppSettingsRepositoryImpl(database) { getUserId() }
    val debtRepository = DebtRepositoryImpl(database) { getUserId() }
    val expenseCycleRepository: ExpenseCycleRepository = ExpenseCycleRepositoryImpl(database) { getUserId() }

    // --- Use Cases ---
    val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    val addTransactionUseCase = AddTransactionUseCase(transactionRepository)
    val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository)
    val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)

    val getAccountsUseCase = GetAccountsUseCase(accountRepository)
    val addAccountUseCase = AddAccountUseCase(accountRepository)
    val updateAccountUseCase = UpdateAccountUseCase(accountRepository)
    val deleteAccountUseCase = DeleteAccountUseCase(accountRepository)

    val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    val addCategoryUseCase = AddCategoryUseCase(categoryRepository)
    val deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

    val getBudgetsUseCase = GetBudgetsUseCase(budgetRepository)
    val upsertBudgetUseCase = UpsertBudgetUseCase(budgetRepository)
    val deleteBudgetUseCase = DeleteBudgetUseCase(budgetRepository)

    val getGoalsUseCase = GetGoalsUseCase(goalRepository)
    val upsertGoalUseCase = UpsertGoalUseCase(goalRepository)
    val deleteGoalUseCase = DeleteGoalUseCase(goalRepository)

    val getRecurringTransactionsUseCase = GetRecurringTransactionsUseCase(recurringTransactionRepository)
    val upsertRecurringTransactionUseCase = UpsertRecurringTransactionUseCase(recurringTransactionRepository)
    val deleteRecurringTransactionUseCase = DeleteRecurringTransactionUseCase(recurringTransactionRepository)

    val getAnalyticsUseCase = GetAnalyticsUseCase(transactionRepository)

    val addDebtUseCase = AddDebtUseCase(debtRepository)
    val getDebtsUseCase = GetDebtsUseCase(debtRepository)
    val deleteDebtUseCase = DeleteDebtUseCase(debtRepository)
    val markDebtPaidUseCase = MarkDebtPaidUseCase(debtRepository)

    val getAppSettingsUseCase = GetAppSettingsUseCase(appSettingsRepository)
    val updateAppSettingsUseCase = UpdateAppSettingsUseCase(appSettingsRepository)

    val getCyclesUseCase = GetCyclesUseCase(expenseCycleRepository)
    val addCycleUseCase = AddCycleUseCase(expenseCycleRepository)
    val markCyclePaidUseCase = MarkCyclePaidUseCase(expenseCycleRepository)
    val deactivateCycleUseCase = DeactivateCycleUseCase(expenseCycleRepository)

    // --- Utility ---
    val db get() = database
    val appContext = context

    companion object {
        @Volatile
        private var _instance: AppContainer? = null

        val instance: AppContainer
            get() = _instance ?: error("AppContainer not initialized. Call init() first.")

        fun init(context: Context) {
            if (_instance == null) {
                synchronized(this) {
                    if (_instance == null) {
                        _instance = AppContainer(context.applicationContext)
                    }
                }
            }
        }
    }
}
