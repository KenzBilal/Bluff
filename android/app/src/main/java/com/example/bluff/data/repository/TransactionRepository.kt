package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.data.local.entity.TransactionEntity
import com.example.bluff.data.remote.dto.TransactionDto
import com.example.bluff.domain.model.Transaction
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getRecentTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Result<String>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(id: String): Result<Unit>
}

class TransactionRepositoryImpl(
    private val db: BluffDatabase,
    private val supabase: SupabaseClient,
    private val userIdProvider: () -> String
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        db.transactionDao().getAllTransactions().map { it.map { e -> e.toModel() } }

    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsByAccount(accountId).map { it.map { e -> e.toModel() } }

    override fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsByCategory(categoryId).map { it.map { e -> e.toModel() } }

    override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsByDateRange(
            startDate.toString(),
            endDate.toString()
        ).map { it.map { e -> e.toModel() } }

    override fun getTransactionsByDate(date: LocalDate): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsByDate(date.toString()).map { it.map { e -> e.toModel() } }

    override fun searchTransactions(query: String): Flow<List<Transaction>> =
        db.transactionDao().searchTransactions(query).map { it.map { e -> e.toModel() } }

    override fun getRecentTransactions(): Flow<List<Transaction>> =
        db.transactionDao().getRecentTransactions().map { it.map { e -> e.toModel() } }

    override suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            val id = if (transaction.id.isEmpty()) UUID.randomUUID().toString() else transaction.id
            val userId = userIdProvider()
            val entity = TransactionEntity.fromModel(transaction.copy(id = id, userId = userId))
            db.transactionDao().insertTransaction(entity)
            queueSync("INSERT", "transactions", id, TransactionDto.fromEntity(entity))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val entity = TransactionEntity.fromModel(transaction)
            db.transactionDao().updateTransaction(entity)
            queueSync("UPDATE", "transactions", transaction.id, TransactionDto.fromEntity(entity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            db.transactionDao().deleteTransaction(id)
            queueSync("DELETE", "transactions", id, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queueSync(op: String, table: String, id: String, payload: TransactionDto?) {
        db.syncQueueDao().insertOperation(
            SyncQueueEntity(
                operationType = op,
                tableName = table,
                entityId = id,
                payload = payload?.let { Json.encodeToString(it) },
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
