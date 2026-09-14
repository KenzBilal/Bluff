package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.CategoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("icon") val icon: String,
    @SerialName("color") val color: String,
    @SerialName("is_system") val isSystem: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: CategoryEntity, userId: String = entity.userId) = CategoryDto(
            id = entity.id,
            userId = userId,
            name = entity.name,
            type = entity.type,
            icon = entity.icon,
            color = entity.color,
            isSystem = entity.isSystem,
            isArchived = entity.isArchived,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
