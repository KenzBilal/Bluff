package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.DebtEntity
import com.example.bluff.domain.model.Debt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface DebtRepository {
    fun getActiveDebts(): Flow<List<Debt>>
    fun getAllDebts(): Flow<List<Debt>>
    suspend fun addDebt(debt: Debt): Result<String>
    suspend fun markPaid(id: String): Result<Unit>
    suspend fun deleteDebt(id: String): Result<Unit>
}

class DebtRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : DebtRepository {

    override fun getActiveDebts(): Flow<List<Debt>> =
        db.debtDao().getActiveDebts().map { it.map { e -> e.toModel() } }

    override fun getAllDebts(): Flow<List<Debt>> =
        db.debtDao().getAllDebts().map { it.map { e -> e.toModel() } }

    override suspend fun addDebt(debt: Debt): Result<String> {
        return try {
            val id = debt.id.ifBlank { UUID.randomUUID().toString() }
            val entity = DebtEntity.fromModel(
                debt.copy(id = id, userId = userIdProvider())
            )
            db.debtDao().insertDebt(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markPaid(id: String): Result<Unit> {
        return try {
            db.debtDao().markPaid(id, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDebt(id: String): Result<Unit> {
        return try {
            db.debtDao().deleteDebt(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
