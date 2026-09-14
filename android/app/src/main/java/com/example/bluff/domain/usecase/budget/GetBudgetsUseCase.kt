package com.example.bluff.domain.usecase.budget

import com.example.bluff.data.repository.BudgetRepository
import com.example.bluff.domain.model.Budget
import kotlinx.coroutines.flow.Flow

class GetBudgetsUseCase(private val repository: BudgetRepository) {
    fun getAll(): Flow<List<Budget>> = repository.getAllBudgets()
    fun getActive(): Flow<List<Budget>> = repository.getActiveBudgets()
    fun getOverallBudget(): Flow<Budget?> = repository.getOverallBudget()
}
