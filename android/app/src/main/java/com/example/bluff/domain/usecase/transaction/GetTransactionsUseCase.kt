package com.example.bluff.domain.usecase.transaction

import com.example.bluff.data.repository.TransactionRepository
import com.example.bluff.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    fun getAll(): Flow<List<Transaction>> = repository.getAllTransactions()
    fun getByAccount(accountId: String): Flow<List<Transaction>> = repository.getTransactionsByAccount(accountId)
    fun getByCategory(categoryId: String): Flow<List<Transaction>> = repository.getTransactionsByCategory(categoryId)
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        repository.getTransactionsByDateRange(startDate, endDate)
    fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>> =
        repository.getTransactionsByDate(date)
    fun search(query: String): Flow<List<Transaction>> = repository.searchTransactions(query)
    fun getRecent(): Flow<List<Transaction>> = repository.getRecentTransactions()
}
