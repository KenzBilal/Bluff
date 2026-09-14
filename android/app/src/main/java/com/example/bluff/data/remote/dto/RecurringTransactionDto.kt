package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.RecurringTransactionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecurringTransactionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: Long,
    @SerialName("type") val type: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("frequency") val frequency: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("next_run_date") val nextRunDate: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("note") val note: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: RecurringTransactionEntity) = RecurringTransactionDto(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            amount = entity.amount,
            type = entity.type,
            accountId = entity.accountId,
            categoryId = entity.categoryId,
            frequency = entity.frequency,
            startDate = entity.startDate,
            endDate = entity.endDate,
            nextRunDate = entity.nextRunDate,
            isActive = entity.isActive,
            note = entity.note,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
