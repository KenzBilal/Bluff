package com.example.bluff.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _transactionsForMonth = MutableStateFlow<List<Transaction>>(emptyList())
    val transactionsForMonth: StateFlow<List<Transaction>> = _transactionsForMonth.asStateFlow()

    private val _transactionsForDay = MutableStateFlow<List<Transaction>>(emptyList())
    val transactionsForDay: StateFlow<List<Transaction>> = _transactionsForDay.asStateFlow()

    private val _dailySummary = MutableStateFlow(DailySummary())
    val dailySummary: StateFlow<DailySummary> = _dailySummary.asStateFlow()

    init {
        loadMonth(_currentMonth.value)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        loadMonth(_currentMonth.value)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        loadMonth(_currentMonth.value)
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadDayDetails(date)
    }

    private fun loadMonth(month: YearMonth) {
        viewModelScope.launch {
            val startDate = month.atDay(1)
            val endDate = month.atEndOfMonth()
            getTransactionsUseCase.getByDateRange(startDate, endDate).collect {
                _transactionsForMonth.value = it
            }
        }
    }

    private fun loadDayDetails(date: LocalDate) {
        viewModelScope.launch {
            getTransactionsUseCase.getTransactionsByDate(date).collect { transactions ->
                _transactionsForDay.value = transactions
                _dailySummary.value = calculateDailySummary(transactions)
            }
        }
    }

    private fun calculateDailySummary(transactions: List<Transaction>): DailySummary {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
        val transfer = transactions.filter { it.type == TransactionType.TRANSFER }.sumOf { it.amountMinor }
        return DailySummary(income = income, expense = expense, transfer = transfer)
    }

    fun getDaysWithTransactions(): Set<Int> {
        return _transactionsForMonth.value
            .map { it.transactionDate.dayOfMonth }
            .toSet()
    }

    data class DailySummary(
        val income: Long = 0L,
        val expense: Long = 0L,
        val transfer: Long = 0L
    ) {
        val net: Long get() = income - expense
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                CalendarViewModel(container.getTransactionsUseCase)
            }
        }
    }
}
