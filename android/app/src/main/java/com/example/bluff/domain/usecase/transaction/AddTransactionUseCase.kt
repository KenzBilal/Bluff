package com.example.bluff.domain.usecase.transaction

import com.example.bluff.data.repository.TransactionRepository
import com.example.bluff.domain.model.Transaction

class AddTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Result<String> {
        if (transaction.amountMinor <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        return repository.addTransaction(transaction)
    }
}
