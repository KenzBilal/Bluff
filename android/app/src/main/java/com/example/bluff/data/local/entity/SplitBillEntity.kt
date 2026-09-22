package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "split_bills",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId", unique = true)]
)
data class SplitBillEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val totalAmountMinor: Long,
    val createdAt: Long = System.currentTimeMillis()
)
