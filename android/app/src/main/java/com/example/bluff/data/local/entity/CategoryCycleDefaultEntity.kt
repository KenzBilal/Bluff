package com.example.bluff.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_cycle_defaults")
data class CategoryCycleDefaultEntity(
    @PrimaryKey val categoryId: String,
    @ColumnInfo(name = "categoryName") val categoryName: String,
    @ColumnInfo(name = "defaultCycleDays") val defaultCycleDays: Int
)
