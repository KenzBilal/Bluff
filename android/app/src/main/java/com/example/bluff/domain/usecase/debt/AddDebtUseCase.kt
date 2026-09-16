package com.example.bluff.domain.usecase.debt

import com.example.bluff.data.repository.DebtRepository
import com.example.bluff.domain.model.Debt

class AddDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(debt: Debt): Result<String> = repository.addDebt(debt)
}
