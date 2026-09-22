package com.example.bluff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bluff.data.local.entity.SplitBillEntity
import com.example.bluff.data.local.entity.SplitBillWithParticipants
import com.example.bluff.data.local.entity.SplitParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitBill(splitBill: SplitBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<SplitParticipantEntity>)

    @Transaction
    @Query("SELECT * FROM split_bills WHERE transactionId = :transactionId LIMIT 1")
    fun getSplitBillByTransactionId(transactionId: String): Flow<SplitBillWithParticipants?>

    @Query("UPDATE split_participants SET isPaid = :isPaid WHERE id = :participantId")
    suspend fun updateParticipantPaidStatus(participantId: String, isPaid: Boolean)
    
    @Query("SELECT * FROM split_participants WHERE id = :participantId LIMIT 1")
    suspend fun getParticipantById(participantId: String): SplitParticipantEntity?
}
