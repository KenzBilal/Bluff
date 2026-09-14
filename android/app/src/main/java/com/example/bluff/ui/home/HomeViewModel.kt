package com.example.bluff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Budget
import com.example.bluff.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _totalBalance = MutableStateFlow(0L)
    val totalBalance: StateFlow<Long> = _totalBalance

    private val _income = MutableStateFlow(0L)
    val income: StateFlow<Long> = _income

    private val _spent = MutableStateFlow(0L)
    val spent: StateFlow<Long> = _spent

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions

    private val _activeBudget = MutableStateFlow<Budget?>(null)
    val activeBudget: StateFlow<Budget?> = _activeBudget

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load dummy data or call usecases
            _totalBalance.value = 842000L
            _income.value = 1200000L
            _spent.value = 358000L
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(AppContainer.instance)
            }
        }
    }
}
