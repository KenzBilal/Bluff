package com.example.bluff.domain.usecase.budget

import com.example.bluff.data.repository.BudgetRepository
import com.example.bluff.domain.model.Budget

class UpsertBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget): Result<String> {
        if (budget.amountMinor <= 0) return Result.failure(IllegalArgumentException("Budget amount must be positive"))
        if (budget.name.isBlank()) return Result.failure(IllegalArgumentException("Budget name cannot be empty"))
        return repository.upsertBudget(budget)
    }
}
