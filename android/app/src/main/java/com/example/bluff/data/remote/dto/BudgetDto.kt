package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.BudgetEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BudgetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: Long,
    @SerialName("period") val period: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: BudgetEntity) = BudgetDto(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            amount = entity.amount,
            period = entity.period,
            categoryId = entity.categoryId,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
