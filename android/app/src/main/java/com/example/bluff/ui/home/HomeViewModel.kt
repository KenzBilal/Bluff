package com.example.bluff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Budget
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.budget.GetBudgetsUseCase
import com.example.bluff.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class HomeViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getBudgetsUseCase: GetBudgetsUseCase
) : ViewModel() {

    private val now = LocalDate.now()
    private val monthStart = now.withDayOfMonth(1)
    private val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth())

    private val _greeting = MutableStateFlow("Good evening")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    val totalBalance: StateFlow<Long> = getAccountsUseCase.getActive()
        .map { accounts -> accounts.sumOf { it.currentBalanceMinor } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyIncome: StateFlow<Long> = getTransactionsUseCase.getByDateRange(monthStart, monthEnd)
        .map { txns -> txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlySpent: StateFlow<Long> = getTransactionsUseCase.getByDateRange(monthStart, monthEnd)
        .map { txns -> txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val recentTransactions: StateFlow<List<Transaction>> = getTransactionsUseCase.getRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBudget: StateFlow<Budget?> = getBudgetsUseCase.getOverallBudget()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        updateGreeting()
    }

    private fun updateGreeting() {
        val hour = LocalTime.now().hour
        _greeting.value = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                HomeViewModel(
                    container.getTransactionsUseCase,
                    container.getAccountsUseCase,
                    container.getBudgetsUseCase
                )
            }
        }
    }
}
