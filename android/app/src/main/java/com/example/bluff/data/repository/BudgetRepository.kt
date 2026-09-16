package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.BudgetEntity
import com.example.bluff.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
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
    private val userIdProvider: () -> String
) : BudgetRepository {

    private val transactionDao = db.transactionDao()

    override fun getAllBudgets(): Flow<List<Budget>> =
        db.budgetDao().getAllBudgets().map { entities ->
            entities.map { e ->
                val budget = e.toModel()
                val spent = transactionDao.getSpendingInRange(
                    budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    budget.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    budget.categoryId
                )
                budget.copy(spentMinor = spent)
            }
        }

    override fun getActiveBudgets(): Flow<List<Budget>> =
        db.budgetDao().getAllBudgets().map { entities ->
            entities.filter { it.isActive }.map { e ->
                val budget = e.toModel()
                val spent = transactionDao.getSpendingInRange(
                    budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    budget.endDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    budget.categoryId
                )
                budget.copy(spentMinor = spent)
            }
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
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        return try {
            db.budgetDao().deleteBudget(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
