package com.example.bluff.domain.model

data class SplitBill(
    val id: String,
    val transactionId: String,
    val totalAmountMinor: Long,
    val participants: List<SplitParticipant>,
    val createdAt: Long = System.currentTimeMillis()
)

data class SplitParticipant(
    val id: String,
    val splitBillId: String,
    val contactName: String,
    val contactPhone: String? = null,
    val amountMinor: Long,
    val isPaid: Boolean = false
)
