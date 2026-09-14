package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.GoalEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoalDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("target_amount") val targetAmount: Long,
    @SerialName("current_amount") val currentAmount: Long = 0L,
    @SerialName("target_date") val targetDate: String? = null,
    @SerialName("icon") val icon: String = "savings",
    @SerialName("color") val color: String = "#6C63FF",
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: GoalEntity) = GoalDto(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            targetAmount = entity.targetAmount,
            currentAmount = entity.currentAmount,
            targetDate = entity.targetDate,
            icon = entity.icon,
            color = entity.color,
            isCompleted = entity.isCompleted,
            isArchived = entity.isArchived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
