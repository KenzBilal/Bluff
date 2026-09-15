package com.example.bluff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bluff.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    fun getByParentId(parentId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    fun getRootCategories(): Flow<List<CategoryEntity>>

    @Query("""
        SELECT DISTINCT c.* FROM categories c
        INNER JOIN transactions t ON t.categoryId = c.id
        ORDER BY t.transactionDate DESC
        LIMIT :limit
    """)
    suspend fun getRecentCategories(limit: Int = 10): List<CategoryEntity>

    @Query("""
        SELECT c.id, COALESCE(SUM(t.amount), 0) as total
        FROM categories c
        LEFT JOIN transactions t ON t.categoryId = c.id
            AND t.transactionDate >= :startOfMonth
            AND t.transactionDate <= :endOfMonth
            AND t.type = 'EXPENSE'
        GROUP BY c.id
    """)
    suspend fun getMonthlySpendByCategory(startOfMonth: String, endOfMonth: String): List<CategorySpendResult>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    suspend fun getRootCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    suspend fun getChildrenOf(parentId: String): List<CategoryEntity>
}

data class CategorySpendResult(
    val id: String,
    val total: Long
)
