package com.example.bluff.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?,
    
    @Relation(
        parentColumn = "toAccountId",
        entityColumn = "id"
    )
    val toAccount: AccountEntity?,
    
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
) {
    fun toModel() = transaction.toModel().copy(
        accountName = account?.name ?: "",
        toAccountName = toAccount?.name,
        categoryName = category?.name,
        categoryIcon = category?.icon,
        categoryIconType = category?.iconType ?: "emoji",
        categoryColor = category?.color
    )
}
