package com.example.bluff.domain.usecase.debt

import com.example.bluff.data.repository.DebtRepository

class DeleteDebtUseCase(private val repository: DebtRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteDebt(id)
}
