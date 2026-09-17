package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import kotlinx.coroutines.flow.Flow

class GetCyclesUseCase(private val repository: ExpenseCycleRepository) {
    operator fun invoke(): Flow<List<ExpenseCycle>> = repository.getAllActive()

    fun getDueSoon(date: String): Flow<List<ExpenseCycle>> = repository.getDueSoon(date)

    suspend fun getByCategoryId(categoryId: String): ExpenseCycle? = repository.getByCategoryId(categoryId)

    suspend fun getDueForReminder(date: String): List<ExpenseCycle> = repository.getDueForReminder(date)
}
