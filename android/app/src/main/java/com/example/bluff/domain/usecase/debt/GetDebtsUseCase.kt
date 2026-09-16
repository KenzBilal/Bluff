package com.example.bluff.domain.usecase.debt

import com.example.bluff.data.repository.DebtRepository
import com.example.bluff.domain.model.Debt
import kotlinx.coroutines.flow.Flow

class GetDebtsUseCase(private val repository: DebtRepository) {
    fun getActive(): Flow<List<Debt>> = repository.getActiveDebts()
    fun getAll(): Flow<List<Debt>> = repository.getAllDebts()
}
