package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.AppSettings

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val userId: String,
    val userName: String = "",
    val phoneNumber: String = "",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val financialMonthStartDay: Int = 1,
    val defaultAccountId: String? = null,
    val accentColor: String = "#6C63FF",
    val animationIntensity: String = "NORMAL",
    val layoutDensity: String = "COMFORTABLE",
    val notificationsBudget: Boolean = true,
    val notificationsRecurring: Boolean = true,
    val notificationsGoals: Boolean = true,
    val notificationsDailyReminder: Boolean = false,
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false
) {
    fun toModel() = AppSettings(
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
        fun fromModel(model: AppSettings) = AppSettingsEntity(
            userId = model.userId,
            userName = model.userName,
            phoneNumber = model.phoneNumber,
            currencyCode = model.currencyCode,
            currencySymbol = model.currencySymbol,
            financialMonthStartDay = model.financialMonthStartDay,
            defaultAccountId = model.defaultAccountId,
            accentColor = model.accentColor,
            animationIntensity = model.animationIntensity,
            layoutDensity = model.layoutDensity,
            notificationsBudget = model.notificationsBudget,
            notificationsRecurring = model.notificationsRecurring,
            notificationsGoals = model.notificationsGoals,
            notificationsDailyReminder = model.notificationsDailyReminder,
            pinEnabled = model.pinEnabled,
            biometricEnabled = model.biometricEnabled
        )
    }
}
