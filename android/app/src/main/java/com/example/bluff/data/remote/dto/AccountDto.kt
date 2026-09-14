package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.AccountEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("currency_code") val currencyCode: String = "INR",
    @SerialName("initial_balance") val initialBalance: Long = 0L,
    @SerialName("icon") val icon: String = "wallet",
    @SerialName("color") val color: String = "#6C63FF",
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: AccountEntity) = AccountDto(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            type = entity.type,
            currencyCode = entity.currencyCode,
            initialBalance = entity.initialBalance,
            icon = entity.icon,
            color = entity.color,
            isArchived = entity.isArchived,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
