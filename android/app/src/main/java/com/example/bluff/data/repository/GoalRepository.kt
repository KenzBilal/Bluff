package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.GoalEntity
import com.example.bluff.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoals(): Flow<List<Goal>>
    suspend fun upsertGoal(goal: Goal): Result<String>
    suspend fun deleteGoal(id: String): Result<Unit>
}

class GoalRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> =
        db.goalDao().getAllGoals().map { it.map { e -> e.toModel() } }

    override fun getActiveGoals(): Flow<List<Goal>> =
        db.goalDao().getAllGoals().map { entities ->
            entities.filter { !it.isArchived && !it.isCompleted }.map { it.toModel() }
        }

    override suspend fun upsertGoal(goal: Goal): Result<String> {
        return try {
            val id = if (goal.id.isEmpty()) UUID.randomUUID().toString() else goal.id
            val userId = userIdProvider()
            val entity = GoalEntity.fromModel(goal.copy(id = id, userId = userId))
            db.goalDao().insertGoal(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        return try {
            db.goalDao().deleteGoal(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
