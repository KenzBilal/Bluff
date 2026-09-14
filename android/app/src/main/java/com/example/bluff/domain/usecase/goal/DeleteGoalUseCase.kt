package com.example.bluff.domain.usecase.goal

import com.example.bluff.data.repository.GoalRepository

class DeleteGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goalId: String): Result<Unit> =
        repository.deleteGoal(goalId)
}
