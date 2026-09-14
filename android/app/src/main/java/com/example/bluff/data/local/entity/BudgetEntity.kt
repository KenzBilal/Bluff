package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Budget
import com.example.bluff.domain.model.BudgetPeriod
import java.time.LocalDate

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val amount: Long,               // amountMinor in domain
    val period: String,             // BudgetPeriod name
    val categoryId: String? = null,
    val startDate: String,          // LocalDate as ISO string
    val endDate: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Budget(
        id = id,
        userId = userId,
        name = name,
        amountMinor = amount,
        period = BudgetPeriod.valueOf(period),
        categoryId = categoryId,
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Budget) = BudgetEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            amount = model.amountMinor,
            period = model.period.name,
            categoryId = model.categoryId,
            startDate = model.startDate.toString(),
            endDate = model.endDate?.toString(),
            isActive = model.isActive,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
