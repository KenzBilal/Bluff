package com.example.bluff.domain.model

data class AppSettings(
    val userId: String,
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
)
