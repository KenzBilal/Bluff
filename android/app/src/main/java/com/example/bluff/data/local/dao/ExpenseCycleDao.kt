package com.example.bluff.data.local.dao

import androidx.room.*
import com.example.bluff.data.local.entity.ExpenseCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCycleDao {
    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getAllActive(): Flow<List<ExpenseCycleEntity>>

    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 AND nextDueDate <= :date ORDER BY nextDueDate ASC")
    fun getDueSoon(date: String): Flow<List<ExpenseCycleEntity>>

    @Query("SELECT * FROM expense_cycles WHERE categoryId = :categoryId AND isActive = 1")
    suspend fun getByCategoryId(categoryId: String): ExpenseCycleEntity?

    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 AND nextDueDate <= :date")
    suspend fun getDueForReminder(date: String): List<ExpenseCycleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: ExpenseCycleEntity)

    @Update
    suspend fun update(cycle: ExpenseCycleEntity)

    @Query("UPDATE expense_cycles SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deactivate(id: String, now: Long = System.currentTimeMillis())
}
