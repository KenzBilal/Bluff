package com.example.bluff.domain.repository

import com.example.bluff.domain.model.CategoryCycleDefault
import com.example.bluff.domain.model.ExpenseCycle
import kotlinx.coroutines.flow.Flow

interface ExpenseCycleRepository {
    fun getAllActive(): Flow<List<ExpenseCycle>>
    fun getDueSoon(date: String): Flow<List<ExpenseCycle>>
    suspend fun getByCategoryId(categoryId: String): ExpenseCycle?
    suspend fun getDueForReminder(date: String): List<ExpenseCycle>
    suspend fun add(cycle: ExpenseCycle)
    suspend fun update(cycle: ExpenseCycle)
    suspend fun deactivate(id: String)
    suspend fun getDefaults(): List<CategoryCycleDefault>
    suspend fun getDefaultForCategory(categoryId: String): CategoryCycleDefault?
}
