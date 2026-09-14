package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Goal
import java.time.LocalDate

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val targetAmount: Long,         // targetAmountMinor in domain
    val currentAmount: Long = 0L,   // currentAmountMinor in domain
    val targetDate: String? = null, // LocalDate as ISO string, nullable
    val icon: String = "savings",
    val color: String = "#6C63FF",
    val isCompleted: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Goal(
        id = id,
        userId = userId,
        name = name,
        targetAmountMinor = targetAmount,
        currentAmountMinor = currentAmount,
        targetDate = targetDate?.let { LocalDate.parse(it) },
        icon = icon,
        color = color,
        isCompleted = isCompleted,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Goal) = GoalEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            targetAmount = model.targetAmountMinor,
            currentAmount = model.currentAmountMinor,
            targetDate = model.targetDate?.toString(),
            icon = model.icon,
            color = model.color,
            isCompleted = model.isCompleted,
            isArchived = model.isArchived,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
