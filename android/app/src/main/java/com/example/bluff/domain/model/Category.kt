package com.example.bluff.domain.model

/**
 * Domain model for a transaction category.
 */
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    val parentId: String? = null,
    val quickAmounts: List<Long> = emptyList(),
    val children: List<Category> = emptyList(),
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
