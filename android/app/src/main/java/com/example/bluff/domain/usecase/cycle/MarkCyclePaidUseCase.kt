package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import java.time.LocalDate

class MarkCyclePaidUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(cycle: ExpenseCycle) {
        val today = LocalDate.now()
        val updated = cycle.copy(
            lastTransactionDate = today,
            nextDueDate = today.plusDays(cycle.cycleDays.toLong())
        )
        repository.update(updated)
    }
}
