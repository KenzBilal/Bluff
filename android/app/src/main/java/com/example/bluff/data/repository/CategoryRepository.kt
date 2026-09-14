package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.CategoryEntity
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.data.remote.dto.CategoryDto
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>>
    fun getExpenseCategories(): Flow<List<Category>>
    fun getIncomeCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category): Result<String>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun seedDefaultCategoriesIfNeeded()
}

class CategoryRepositoryImpl(
    private val db: BluffDatabase,
    private val supabase: SupabaseClient,
    private val userIdProvider: () -> String
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        db.categoryDao().getAllCategories().map { entities -> entities.map { it.toModel() } }

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        db.categoryDao().getAllCategories().map { entities ->
            entities.filter { it.type == type.name || it.type == CategoryType.BOTH.name }
                .map { it.toModel() }
        }

    override fun getExpenseCategories(): Flow<List<Category>> =
        getCategoriesByType(CategoryType.EXPENSE)

    override fun getIncomeCategories(): Flow<List<Category>> =
        getCategoriesByType(CategoryType.INCOME)

    override suspend fun addCategory(category: Category): Result<String> {
        return try {
            val id = if (category.id.isEmpty()) UUID.randomUUID().toString() else category.id
            val userId = userIdProvider()
            val entity = CategoryEntity.fromModel(category.copy(id = id, userId = userId))
            db.categoryDao().insertCategory(entity)
            queueSync("INSERT", "categories", id, CategoryDto.fromEntity(entity, userId))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            val entity = CategoryEntity.fromModel(category)
            db.categoryDao().updateCategory(entity)
            queueSync("UPDATE", "categories", category.id, CategoryDto.fromEntity(entity, category.userId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun seedDefaultCategoriesIfNeeded() {
        val count = db.categoryDao().getCount()
        if (count > 0) return

        val userId = userIdProvider()
        val defaults = listOf(
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Food", type = CategoryType.EXPENSE, icon = "🍽️", color = "#FF6B6B", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Transport", type = CategoryType.EXPENSE, icon = "🚌", color = "#4ECDC4", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Shopping", type = CategoryType.EXPENSE, icon = "🛍️", color = "#45B7D1", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "College", type = CategoryType.EXPENSE, icon = "📚", color = "#96CEB4", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Entertainment", type = CategoryType.EXPENSE, icon = "🎮", color = "#FFEAA7", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Bills", type = CategoryType.EXPENSE, icon = "💡", color = "#DDA0DD", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Health", type = CategoryType.EXPENSE, icon = "🏥", color = "#98FB98", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Subscriptions", type = CategoryType.EXPENSE, icon = "📱", color = "#F0E68C", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Salary", type = CategoryType.INCOME, icon = "💰", color = "#00C896", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Freelance", type = CategoryType.INCOME, icon = "💻", color = "#3A8EFF", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Other", type = CategoryType.BOTH, icon = "📦", color = "#888888", isSystem = true)
        )
        defaults.forEach { addCategory(it) }
    }

    private suspend fun queueSync(op: String, table: String, id: String, payload: CategoryDto?) {
        db.syncQueueDao().insertOperation(
            SyncQueueEntity(
                operationType = op,
                tableName = table,
                entityId = id,
                payload = payload?.let { Json.encodeToString(it) },
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
