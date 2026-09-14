package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.AppSettingsEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("currency") val currency: String,
    @SerialName("theme") val theme: String,
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean,
    @SerialName("sync_enabled") val syncEnabled: Boolean,
    @SerialName("updated_at") val updatedAt: String
) {
    fun toEntity() = AppSettingsEntity(
        id = id,
        userId = userId,
        currency = currency,
        theme = theme,
        notificationsEnabled = notificationsEnabled,
        syncEnabled = syncEnabled,
        updatedAt = updatedAt
    )

    companion object {
        fun fromEntity(entity: AppSettingsEntity, userId: String) = AppSettingsDto(
            id = entity.id,
            userId = userId,
            currency = entity.currency,
            theme = entity.theme,
            notificationsEnabled = entity.notificationsEnabled,
            syncEnabled = entity.syncEnabled,
            updatedAt = entity.updatedAt
        )
    }
}
