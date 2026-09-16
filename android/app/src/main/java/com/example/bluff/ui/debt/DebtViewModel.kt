package com.example.bluff.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Debt
import com.example.bluff.domain.model.DebtDirection
import com.example.bluff.domain.usecase.debt.DeleteDebtUseCase
import com.example.bluff.domain.usecase.debt.GetDebtsUseCase
import com.example.bluff.domain.usecase.debt.MarkDebtPaidUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DebtViewModel(
    private val getDebtsUseCase: GetDebtsUseCase,
    private val markDebtPaidUseCase: MarkDebtPaidUseCase,
    private val deleteDebtUseCase: DeleteDebtUseCase
) : ViewModel() {

    val activeDebts: StateFlow<List<Debt>> = getDebtsUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val theyOweMe: StateFlow<List<Debt>> = getDebtsUseCase.getActive()
        .map { list -> list.filter { it.direction == DebtDirection.THEY_OWE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val iOwe: StateFlow<List<Debt>> = getDebtsUseCase.getActive()
        .map { list -> list.filter { it.direction == DebtDirection.I_OWE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTheyOweMe: StateFlow<Long> = getDebtsUseCase.getActive()
        .map { list -> list.filter { it.direction == DebtDirection.THEY_OWE }.sumOf { it.amountMinor } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalIOwe: StateFlow<Long> = getDebtsUseCase.getActive()
        .map { list -> list.filter { it.direction == DebtDirection.I_OWE }.sumOf { it.amountMinor } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun markPaid(id: String) {
        viewModelScope.launch { markDebtPaidUseCase(id) }
    }

    fun delete(id: String) {
        viewModelScope.launch { deleteDebtUseCase(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = AppContainer.instance
                DebtViewModel(c.getDebtsUseCase, c.markDebtPaidUseCase, c.deleteDebtUseCase)
            }
        }
    }
}
