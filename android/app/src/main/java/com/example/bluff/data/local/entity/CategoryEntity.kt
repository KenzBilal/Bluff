package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val type: String, // stored as String, converted via TypeConverter
    val icon: String,
    val color: String,
    val parentId: String? = null,
    val quickAmounts: String = "[]",
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Category(
        id = id,
        userId = userId,
        name = name,
        icon = icon,
        color = color,
        type = CategoryType.valueOf(type),
        parentId = parentId,
        quickAmounts = quickAmounts.removeSurrounding("[", "]")
            .split(",")
            .filter { it.isNotBlank() }
            .map { it.trim().toLongOrNull() ?: 0L },
        isSystem = isSystem,
        isArchived = isArchived,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Category) = CategoryEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            type = model.type.name,
            icon = model.icon,
            color = model.color,
            parentId = model.parentId,
            quickAmounts = model.quickAmounts.joinToString(","),
            isSystem = model.isSystem,
            isArchived = model.isArchived,
            sortOrder = model.sortOrder,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
