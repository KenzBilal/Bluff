package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.TransactionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("amount") val amount: Long,
    @SerialName("currency_code") val currencyCode: String = "INR",
    @SerialName("type") val type: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("to_account_id") val toAccountId: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("recurring_id") val recurringId: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: TransactionEntity) = TransactionDto(
            id = entity.id,
            userId = entity.userId,
            amount = entity.amount,
            currencyCode = entity.currencyCode,
            type = entity.type,
            accountId = entity.accountId,
            toAccountId = entity.toAccountId,
            categoryId = entity.categoryId,
            note = entity.note,
            transactionDate = entity.transactionDate,
            recurringId = entity.recurringId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
