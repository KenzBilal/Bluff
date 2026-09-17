package com.example.bluff.data.local.dao

import androidx.room.*
import com.example.bluff.data.local.entity.CategoryCycleDefaultEntity

@Dao
interface CategoryCycleDefaultDao {
    @Query("SELECT * FROM category_cycle_defaults")
    suspend fun getAll(): List<CategoryCycleDefaultEntity>

    @Query("SELECT * FROM category_cycle_defaults WHERE categoryId = :categoryId")
    suspend fun getByCategoryId(categoryId: String): CategoryCycleDefaultEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(defaults: List<CategoryCycleDefaultEntity>)
}
