package com.example.bluff.data.remote.dto

import com.example.bluff.data.local.entity.AppSettingsEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("currency_code") val currencyCode: String = "INR",
    @SerialName("currency_symbol") val currencySymbol: String = "₹",
    @SerialName("financial_month_start_day") val financialMonthStartDay: Int = 1,
    @SerialName("default_account_id") val defaultAccountId: String? = null,
    @SerialName("accent_color") val accentColor: String = "#6C63FF",
    @SerialName("animation_intensity") val animationIntensity: String = "NORMAL",
    @SerialName("layout_density") val layoutDensity: String = "COMFORTABLE",
    @SerialName("notifications_budget") val notificationsBudget: Boolean = true,
    @SerialName("notifications_recurring") val notificationsRecurring: Boolean = true,
    @SerialName("notifications_goals") val notificationsGoals: Boolean = true,
    @SerialName("notifications_daily_reminder") val notificationsDailyReminder: Boolean = false,
    @SerialName("pin_enabled") val pinEnabled: Boolean = false,
    @SerialName("biometric_enabled") val biometricEnabled: Boolean = false
) {
    fun toEntity() = AppSettingsEntity(
        userId = userId,
        userName = userName,
        phoneNumber = phoneNumber,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        financialMonthStartDay = financialMonthStartDay,
        defaultAccountId = defaultAccountId,
        accentColor = accentColor,
        animationIntensity = animationIntensity,
        layoutDensity = layoutDensity,
        notificationsBudget = notificationsBudget,
        notificationsRecurring = notificationsRecurring,
        notificationsGoals = notificationsGoals,
        notificationsDailyReminder = notificationsDailyReminder,
        pinEnabled = pinEnabled,
        biometricEnabled = biometricEnabled
    )

    companion object {
        fun fromEntity(entity: AppSettingsEntity) = AppSettingsDto(
            userId = entity.userId,
            userName = entity.userName,
            phoneNumber = entity.phoneNumber,
            currencyCode = entity.currencyCode,
            currencySymbol = entity.currencySymbol,
            financialMonthStartDay = entity.financialMonthStartDay,
            defaultAccountId = entity.defaultAccountId,
            accentColor = entity.accentColor,
            animationIntensity = entity.animationIntensity,
            layoutDensity = entity.layoutDensity,
            notificationsBudget = entity.notificationsBudget,
            notificationsRecurring = entity.notificationsRecurring,
            notificationsGoals = entity.notificationsGoals,
            notificationsDailyReminder = entity.notificationsDailyReminder,
            pinEnabled = entity.pinEnabled,
            biometricEnabled = entity.biometricEnabled
        )
    }
}
