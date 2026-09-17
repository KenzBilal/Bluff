package com.example.bluff.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_cycles",
    indices = [Index(value = ["categoryId"], unique = true)]
)
data class ExpenseCycleEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    @ColumnInfo(name = "amountMinor") val amountMinor: Long,
    @ColumnInfo(name = "cycleDays") val cycleDays: Int,
    @ColumnInfo(name = "lastTransactionDate") val lastTransactionDate: String,
    @ColumnInfo(name = "nextDueDate") val nextDueDate: String,
    @ColumnInfo(name = "isActive") val isActive: Boolean = true,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long
)
