package com.example.bluff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bluff.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY createdAt DESC")
    fun getActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: String): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity)

    @Query("UPDATE debts SET isPaid = 1, paidAt = :paidAt WHERE id = :id")
    suspend fun markPaid(id: String, paidAt: Long)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebt(id: String)

    @Query("DELETE FROM debts")
    suspend fun deleteAll()
}
