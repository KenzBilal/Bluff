package com.example.bluff.domain.usecase.goal

import com.example.bluff.data.repository.GoalRepository
import com.example.bluff.domain.model.Goal
import kotlinx.coroutines.flow.Flow

class GetGoalsUseCase(private val repository: GoalRepository) {
    fun getAll(): Flow<List<Goal>> = repository.getAllGoals()
    fun getActive(): Flow<List<Goal>> = repository.getActiveGoals()
}
