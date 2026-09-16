package com.example.bluff.domain.model

data class Debt(
    val id: String,
    val userId: String,
    val amountMinor: Long,              // in paise
    val direction: DebtDirection,       // I_OWE or THEY_OWE
    val contactName: String,            // from phone contact or manually entered
    val contactPhone: String? = null,
    val note: String? = null,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)
