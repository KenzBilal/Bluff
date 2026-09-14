package com.example.bluff.domain.usecase.recurring

import com.example.bluff.data.repository.RecurringTransactionRepository
import com.example.bluff.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class GetRecurringTransactionsUseCase(private val repository: RecurringTransactionRepository) {
    fun getAll(): Flow<List<RecurringTransaction>> = repository.getAllRecurringTransactions()
    fun getActive(): Flow<List<RecurringTransaction>> = repository.getActiveRecurringTransactions()
}

class UpsertRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    suspend operator fun invoke(recurring: RecurringTransaction): Result<String> {
        if (recurring.amountMinor <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        if (recurring.name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        return repository.upsertRecurringTransaction(recurring)
    }
}

class DeleteRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteRecurringTransaction(id)
}
