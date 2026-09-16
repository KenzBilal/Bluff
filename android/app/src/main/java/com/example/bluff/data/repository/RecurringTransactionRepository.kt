package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.RecurringTransactionEntity
import com.example.bluff.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface RecurringTransactionRepository {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>>
    suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransaction): Result<String>
    suspend fun deleteRecurringTransaction(id: String): Result<Unit>
}

class RecurringTransactionRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : RecurringTransactionRepository {

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> =
        db.recurringTransactionDao().getAllRecurringTransactions().map { it.map { e -> e.toModel() } }

    override fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>> =
        db.recurringTransactionDao().getAllRecurringTransactions().map { entities ->
            entities.filter { it.isActive }.map { it.toModel() }
        }

    override suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransaction): Result<String> {
        return try {
            val id = if (recurringTransaction.id.isEmpty()) UUID.randomUUID().toString() else recurringTransaction.id
            val userId = userIdProvider()
            val entity = RecurringTransactionEntity.fromModel(recurringTransaction.copy(id = id, userId = userId))
            db.recurringTransactionDao().insertRecurringTransaction(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecurringTransaction(id: String): Result<Unit> {
        return try {
            db.recurringTransactionDao().deleteRecurringTransaction(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
