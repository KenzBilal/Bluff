package com.example.bluff.domain.usecase.recurring

import com.example.bluff.data.repository.RecurringTransactionRepository
import com.example.bluff.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.util.RecurrenceUtil
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

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

class PayRecurringTransactionUseCase(
    private val recurringRepository: RecurringTransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) {
    suspend operator fun invoke(recurring: RecurringTransaction): Result<Unit> {
        return try {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = recurring.userId,
                accountId = recurring.accountId,
                categoryId = recurring.categoryId,
                amountMinor = recurring.amountMinor,
                type = recurring.type,
                note = recurring.name,
                transactionDate = LocalDate.now(),
                recurringId = recurring.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val addResult = addTransactionUseCase(transaction)
            if (addResult.isFailure) return Result.failure(addResult.exceptionOrNull() ?: Exception("Failed to add transaction"))

            val nextDate = RecurrenceUtil.calculateNextRunDate(recurring.nextRunDate, recurring.frequency)
            val updatedRecurring = recurring.copy(nextRunDate = nextDate, updatedAt = System.currentTimeMillis())
            val upsertResult = recurringRepository.upsertRecurringTransaction(updatedRecurring)
            if (upsertResult.isFailure) return Result.failure(upsertResult.exceptionOrNull() ?: Exception("Failed to update recurring"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
