package com.example.bluff.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SplitBillWithParticipants(
    @Embedded val splitBill: SplitBillEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "splitBillId"
    )
    val participants: List<SplitParticipantEntity>
)
