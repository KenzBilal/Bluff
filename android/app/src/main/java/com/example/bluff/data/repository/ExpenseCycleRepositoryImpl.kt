package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.ExpenseCycleEntity
import com.example.bluff.domain.model.CategoryCycleDefault
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseCycleRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : ExpenseCycleRepository {

    private val cycleDao = db.expenseCycleDao()
    private val defaultDao = db.categoryCycleDefaultDao()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllActive(): Flow<List<ExpenseCycle>> {
        return cycleDao.getAllActive().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getDueSoon(date: String): Flow<List<ExpenseCycle>> {
        return cycleDao.getDueSoon(date).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getByCategoryId(categoryId: String): ExpenseCycle? {
        return cycleDao.getByCategoryId(categoryId)?.toModel()
    }

    override suspend fun getDueForReminder(date: String): List<ExpenseCycle> {
        return cycleDao.getDueForReminder(date).map { it.toModel() }
    }

    override suspend fun add(cycle: ExpenseCycle) {
        cycleDao.insert(cycle.toEntity())
    }

    override suspend fun update(cycle: ExpenseCycle) {
        cycleDao.update(cycle.toEntity())
    }

    override suspend fun deactivate(id: String) {
        cycleDao.deactivate(id)
    }

    override suspend fun getDefaults(): List<CategoryCycleDefault> {
        return defaultDao.getAll().map {
            CategoryCycleDefault(it.categoryId, it.categoryName, it.defaultCycleDays)
        }
    }

    override suspend fun getDefaultForCategory(categoryId: String): CategoryCycleDefault? {
        return defaultDao.getByCategoryId(categoryId)?.let {
            CategoryCycleDefault(it.categoryId, it.categoryName, it.defaultCycleDays)
        }
    }

    private fun ExpenseCycleEntity.toModel() = ExpenseCycle(
        id = id,
        categoryId = categoryId,
        name = name,
        amountMinor = amountMinor,
        cycleDays = cycleDays,
        lastTransactionDate = LocalDate.parse(lastTransactionDate, formatter),
        nextDueDate = LocalDate.parse(nextDueDate, formatter),
        isActive = isActive
    )

    private fun ExpenseCycle.toEntity() = ExpenseCycleEntity(
        id = id,
        categoryId = categoryId,
        name = name,
        amountMinor = amountMinor,
        cycleDays = cycleDays,
        lastTransactionDate = lastTransactionDate.format(formatter),
        nextDueDate = nextDueDate.format(formatter),
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
