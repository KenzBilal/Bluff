package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.model.RecurringTransaction
import com.example.bluff.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val amount: Long,               // amountMinor in domain
    val type: String,               // TransactionType name
    val accountId: String,
    val categoryId: String? = null,
    val frequency: String,          // RecurrenceFrequency name
    val startDate: String,          // LocalDate ISO string
    val endDate: String? = null,
    val nextRunDate: String,        // LocalDate ISO string
    val isActive: Boolean = true,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = RecurringTransaction(
        id = id,
        userId = userId,
        name = name,
        amountMinor = amount,
        type = TransactionType.valueOf(type),
        accountId = accountId,
        categoryId = categoryId,
        frequency = RecurrenceFrequency.valueOf(frequency),
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        nextRunDate = LocalDate.parse(nextRunDate),
        isActive = isActive,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: RecurringTransaction) = RecurringTransactionEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            amount = model.amountMinor,
            type = model.type.name,
            accountId = model.accountId,
            categoryId = model.categoryId,
            frequency = model.frequency.name,
            startDate = model.startDate.toString(),
            endDate = model.endDate?.toString(),
            nextRunDate = model.nextRunDate.toString(),
            isActive = model.isActive,
            note = model.note,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
