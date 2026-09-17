package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.repository.ExpenseCycleRepository

class DeactivateCycleUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(cycleId: String) {
        repository.deactivate(cycleId)
    }
}
