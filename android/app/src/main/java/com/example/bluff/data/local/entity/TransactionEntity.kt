package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Long,               // Room column, maps to amountMinor in domain
    val currencyCode: String = "INR",
    val type: String,               // stored as String via TypeConverter
    val accountId: String,
    val toAccountId: String? = null,
    val categoryId: String? = null,
    val note: String? = null,
    val transactionDate: String,    // stored as "yyyy-MM-dd" via TypeConverter
    val recurringId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Transaction(
        id = id,
        userId = userId,
        amountMinor = amount,
        currencyCode = currencyCode,
        type = TransactionType.valueOf(type),
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = note,
        transactionDate = LocalDate.parse(transactionDate),
        recurringId = recurringId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Transaction) = TransactionEntity(
            id = model.id,
            userId = model.userId,
            amount = model.amountMinor,
            currencyCode = model.currencyCode,
            type = model.type.name,
            accountId = model.accountId,
            toAccountId = model.toAccountId,
            categoryId = model.categoryId,
            note = model.note,
            transactionDate = model.transactionDate.toString(),
            recurringId = model.recurringId,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
