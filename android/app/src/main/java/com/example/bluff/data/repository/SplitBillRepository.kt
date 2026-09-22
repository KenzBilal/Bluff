package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.SplitBillEntity
import com.example.bluff.data.local.entity.SplitParticipantEntity
import com.example.bluff.domain.model.SplitBill
import com.example.bluff.domain.model.SplitParticipant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SplitBillRepository {
    fun getSplitBillByTransactionId(transactionId: String): Flow<SplitBill?>
    suspend fun saveSplitBill(splitBill: SplitBill): Result<Unit>
    suspend fun markParticipantPaid(participantId: String, isPaid: Boolean): Result<Unit>
}

class SplitBillRepositoryImpl(
    private val db: BluffDatabase
) : SplitBillRepository {

    override fun getSplitBillByTransactionId(transactionId: String): Flow<SplitBill?> =
        db.splitBillDao().getSplitBillByTransactionId(transactionId).map { entity ->
            entity?.let {
                SplitBill(
                    id = it.splitBill.id,
                    transactionId = it.splitBill.transactionId,
                    totalAmountMinor = it.splitBill.totalAmountMinor,
                    createdAt = it.splitBill.createdAt,
                    participants = it.participants.map { p ->
                        SplitParticipant(
                            id = p.id,
                            splitBillId = p.splitBillId,
                            contactName = p.contactName,
                            contactPhone = p.contactPhone,
                            amountMinor = p.amountMinor,
                            isPaid = p.isPaid
                        )
                    }
                )
            }
        }

    override suspend fun saveSplitBill(splitBill: SplitBill): Result<Unit> {
        return try {
            val entity = SplitBillEntity(
                id = splitBill.id,
                transactionId = splitBill.transactionId,
                totalAmountMinor = splitBill.totalAmountMinor,
                createdAt = splitBill.createdAt
            )
            val participants = splitBill.participants.map { p ->
                SplitParticipantEntity(
                    id = p.id,
                    splitBillId = p.splitBillId,
                    contactName = p.contactName,
                    contactPhone = p.contactPhone,
                    amountMinor = p.amountMinor,
                    isPaid = p.isPaid
                )
            }
            db.splitBillDao().insertSplitBill(entity)
            db.splitBillDao().insertParticipants(participants)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markParticipantPaid(participantId: String, isPaid: Boolean): Result<Unit> {
        return try {
            db.splitBillDao().updateParticipantPaidStatus(participantId, isPaid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
