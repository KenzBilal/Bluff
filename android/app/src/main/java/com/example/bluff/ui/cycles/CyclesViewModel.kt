package com.example.bluff.ui.cycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.usecase.cycle.DeactivateCycleUseCase
import com.example.bluff.domain.usecase.cycle.GetCyclesUseCase
import com.example.bluff.domain.usecase.cycle.MarkCyclePaidUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CyclesViewModel(
    private val getCyclesUseCase: GetCyclesUseCase,
    private val markCyclePaidUseCase: MarkCyclePaidUseCase,
    private val deactivateCycleUseCase: DeactivateCycleUseCase
) : ViewModel() {

    val cycles = getCyclesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filter = MutableStateFlow(CycleFilter.ALL)

    val filteredCycles: StateFlow<List<ExpenseCycle>> = combine(cycles, filter) { list, f ->
        when (f) {
            CycleFilter.ALL -> list
            CycleFilter.DUE_SOON -> list.filter { it.daysUntilDue in 0..7 }
            CycleFilter.OVERDUE -> list.filter { it.isOverdue }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markPaid(cycle: ExpenseCycle) {
        viewModelScope.launch {
            markCyclePaidUseCase(cycle)
        }
    }

    fun deactivate(cycleId: String) {
        viewModelScope.launch {
            deactivateCycleUseCase(cycleId)
        }
    }

    fun setFilter(f: CycleFilter) {
        filter.value = f
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                CyclesViewModel(
                    container.getCyclesUseCase,
                    container.markCyclePaidUseCase,
                    container.deactivateCycleUseCase
                )
            }
        }
    }
}

enum class CycleFilter { ALL, DUE_SOON, OVERDUE }
