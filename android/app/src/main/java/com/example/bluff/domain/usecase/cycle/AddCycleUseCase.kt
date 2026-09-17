package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import java.time.LocalDate
import java.util.UUID

class AddCycleUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(
        categoryId: String,
        name: String,
        amountMinor: Long,
        cycleDays: Int,
        transactionDate: LocalDate = LocalDate.now()
    ): ExpenseCycle {
        val existing = repository.getByCategoryId(categoryId)
        if (existing != null) {
            val updated = existing.copy(
                amountMinor = amountMinor,
                cycleDays = cycleDays,
                lastTransactionDate = transactionDate,
                nextDueDate = transactionDate.plusDays(cycleDays.toLong())
            )
            repository.update(updated)
            return updated
        }

        val cycle = ExpenseCycle(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            name = name,
            amountMinor = amountMinor,
            cycleDays = cycleDays,
            lastTransactionDate = transactionDate,
            nextDueDate = transactionDate.plusDays(cycleDays.toLong())
        )
        repository.add(cycle)
        return cycle
    }
}
