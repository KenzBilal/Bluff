package com.example.bluff.domain.usecase.goal

import com.example.bluff.data.repository.GoalRepository
import com.example.bluff.domain.model.Goal

class UpsertGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: Goal): Result<String> {
        if (goal.name.isBlank()) return Result.failure(IllegalArgumentException("Goal name cannot be empty"))
        if (goal.targetAmountMinor <= 0) return Result.failure(IllegalArgumentException("Target amount must be positive"))
        if (goal.currentAmountMinor < 0) return Result.failure(IllegalArgumentException("Current amount cannot be negative"))
        return repository.upsertGoal(goal)
    }
}
