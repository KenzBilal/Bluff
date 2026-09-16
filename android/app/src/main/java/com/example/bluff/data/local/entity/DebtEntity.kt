package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Debt
import com.example.bluff.domain.model.DebtDirection

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountMinor: Long,
    val direction: String,          // "I_OWE" or "THEY_OWE"
    val contactName: String,
    val contactPhone: String? = null,
    val note: String? = null,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
) {
    fun toModel() = Debt(
        id = id,
        userId = userId,
        amountMinor = amountMinor,
        direction = DebtDirection.valueOf(direction),
        contactName = contactName,
        contactPhone = contactPhone,
        note = note,
        isPaid = isPaid,
        createdAt = createdAt,
        paidAt = paidAt
    )

    companion object {
        fun fromModel(model: Debt) = DebtEntity(
            id = model.id,
            userId = model.userId,
            amountMinor = model.amountMinor,
            direction = model.direction.name,
            contactName = model.contactName,
            contactPhone = model.contactPhone,
            note = model.note,
            isPaid = model.isPaid,
            createdAt = model.createdAt,
            paidAt = model.paidAt
        )
    }
}
