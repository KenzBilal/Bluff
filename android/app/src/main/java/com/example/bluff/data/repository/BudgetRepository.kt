package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.BudgetEntity
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.data.remote.dto.BudgetDto
import com.example.bluff.domain.model.Budget
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    fun getActiveBudgets(): Flow<List<Budget>>
    fun getOverallBudget(): Flow<Budget?>
    suspend fun upsertBudget(budget: Budget): Result<String>
    suspend fun deleteBudget(id: String): Result<Unit>
}

class BudgetRepositoryImpl(
    private val db: BluffDatabase,
    private val supabase: SupabaseClient,
    private val userIdProvider: () -> String
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<Budget>> =
        db.budgetDao().getAllBudgets().map { it.map { e -> e.toModel() } }

    override fun getActiveBudgets(): Flow<List<Budget>> =
        db.budgetDao().getAllBudgets().map { entities ->
            entities.filter { it.isActive }.map { it.toModel() }
        }

    override fun getOverallBudget(): Flow<Budget?> =
        db.budgetDao().getAllBudgets().map { entities ->
            entities.firstOrNull { it.categoryId == null && it.isActive }?.toModel()
        }

    override suspend fun upsertBudget(budget: Budget): Result<String> {
        return try {
            val id = if (budget.id.isEmpty()) UUID.randomUUID().toString() else budget.id
            val userId = userIdProvider()
            val entity = BudgetEntity.fromModel(budget.copy(id = id, userId = userId))
            db.budgetDao().insertBudget(entity)
            queueSync("UPSERT", "budgets", id, BudgetDto.fromEntity(entity))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        return try {
            db.budgetDao().deleteBudget(id)
            queueSync("DELETE", "budgets", id, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queueSync(op: String, table: String, id: String, payload: BudgetDto?) {
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
