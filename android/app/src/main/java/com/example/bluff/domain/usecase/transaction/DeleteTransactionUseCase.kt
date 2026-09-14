package com.example.bluff.domain.usecase.transaction

import com.example.bluff.data.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transactionId: String): Result<Unit> =
        repository.deleteTransaction(transactionId)
}
