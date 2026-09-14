package com.example.bluff.domain.usecase.budget

import com.example.bluff.data.repository.BudgetRepository

class DeleteBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budgetId: String): Result<Unit> =
        repository.deleteBudget(budgetId)
}
