package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_tags")
data class TransactionTagEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val tagId: String,
    val updatedAt: String
)
