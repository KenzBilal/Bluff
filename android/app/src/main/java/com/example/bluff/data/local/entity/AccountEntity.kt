package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val type: String,           // stored as String via TypeConverter
    val currencyCode: String = "INR",
    val initialBalance: Long = 0L,  // Room column name "initialBalance" = minor units
    val icon: String = "wallet",
    val color: String = "#6C63FF",
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(currentBalance: Long = initialBalance) = Account(
        id = id,
        userId = userId,
        name = name,
        type = AccountType.valueOf(type),
        currencyCode = currencyCode,
        initialBalanceMinor = initialBalance,
        icon = icon,
        color = color,
        isArchived = isArchived,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
        currentBalanceMinor = currentBalance
    )

    companion object {
        fun fromModel(model: Account) = AccountEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            type = model.type.name,
            currencyCode = model.currencyCode,
            initialBalance = model.initialBalanceMinor,
            icon = model.icon,
            color = model.color,
            isArchived = model.isArchived,
            sortOrder = model.sortOrder,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
