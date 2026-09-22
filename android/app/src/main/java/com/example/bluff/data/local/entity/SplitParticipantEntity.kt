package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "split_participants",
    foreignKeys = [
        ForeignKey(
            entity = SplitBillEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitBillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("splitBillId")]
)
data class SplitParticipantEntity(
    @PrimaryKey val id: String,
    val splitBillId: String,
    val contactName: String,
    val contactPhone: String? = null,
    val amountMinor: Long,
    val isPaid: Boolean = false
)
