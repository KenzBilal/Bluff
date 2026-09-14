package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.RecurringTransactionEntity
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.data.remote.dto.RecurringTransactionDto
import com.example.bluff.domain.model.RecurringTransaction
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

interface RecurringTransactionRepository {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>
    fun getActiveRecurringTransactions(): Flow<List<RecurringTransaction>>
    suspend fun upsertRecurringTransaction(recurringTransaction: RecurringTransaction): Result<String>
    suspend fun deleteRecurringTransaction(id: String): Result<Unit>
}

class RecurringTransactionRepositoryImpl(
    private val db: BluffDatabase,
    private val supabase: SupabaseClient,
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
            queueSync("UPSERT", "recurring_transactions", id, RecurringTransactionDto.fromEntity(entity))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecurringTransaction(id: String): Result<Unit> {
        return try {
            db.recurringTransactionDao().deleteRecurringTransaction(id)
            queueSync("DELETE", "recurring_transactions", id, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queueSync(op: String, table: String, id: String, payload: RecurringTransactionDto?) {
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
