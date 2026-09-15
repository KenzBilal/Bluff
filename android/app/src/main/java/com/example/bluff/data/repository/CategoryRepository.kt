package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.CategoryEntity
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.data.remote.dto.CategoryDto
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    suspend fun getCategoryTree(): List<Category>
    suspend fun getRecentCategories(limit: Int = 10): List<Category>
    suspend fun getMonthlySpendByCategory(): Map<String, Long>
    suspend fun getChildrenOf(parentId: String): List<Category>
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

        val foodId = UUID.randomUUID().toString()
        val transportId = UUID.randomUUID().toString()
        val shoppingId = UUID.randomUUID().toString()
        val collegeId = UUID.randomUUID().toString()
        val entertainmentId = UUID.randomUUID().toString()
        val billsId = UUID.randomUUID().toString()
        val healthId = UUID.randomUUID().toString()
        val subscriptionsId = UUID.randomUUID().toString()

        val mealsId = UUID.randomUUID().toString()
        val drinksId = UUID.randomUUID().toString()
        val snacksId = UUID.randomUUID().toString()

        val breakfastId = UUID.randomUUID().toString()
        val lunchId = UUID.randomUUID().toString()
        val dinnerId = UUID.randomUUID().toString()

        val teaCoffeeId = UUID.randomUUID().toString()
        val juiceId = UUID.randomUUID().toString()

        val autoId = UUID.randomUUID().toString()
        val busId = UUID.randomUUID().toString()
        val cabId = UUID.randomUUID().toString()

        val groceriesId = UUID.randomUUID().toString()
        val clothesId = UUID.randomUUID().toString()

        val booksId = UUID.randomUUID().toString()
        val stationeryId = UUID.randomUUID().toString()

        val moviesId = UUID.randomUUID().toString()
        val gamesId = UUID.randomUUID().toString()

        val electricityId = UUID.randomUUID().toString()
        val internetId = UUID.randomUUID().toString()
        val phoneId = UUID.randomUUID().toString()

        val medicineId = UUID.randomUUID().toString()
        val doctorId = UUID.randomUUID().toString()

        val netflixId = UUID.randomUUID().toString()
        val spotifyId = UUID.randomUUID().toString()

        val defaults = listOf(
            Category(id = foodId, userId = userId, name = "Food", type = CategoryType.EXPENSE, icon = "🍽️", color = "#FF6B6B", isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
            Category(id = mealsId, userId = userId, name = "Meals", type = CategoryType.EXPENSE, icon = "🍛", color = "#FF6B6B", parentId = foodId, isSystem = true),
            Category(id = breakfastId, userId = userId, name = "Breakfast", type = CategoryType.EXPENSE, icon = "🌅", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(3000, 5000)),
            Category(id = lunchId, userId = userId, name = "Lunch", type = CategoryType.EXPENSE, icon = "🍛", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(6000, 8000, 10000)),
            Category(id = dinnerId, userId = userId, name = "Dinner", type = CategoryType.EXPENSE, icon = "🌙", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(8000, 10000, 12000)),
            Category(id = drinksId, userId = userId, name = "Drinks", type = CategoryType.EXPENSE, icon = "☕", color = "#FF6B6B", parentId = foodId, isSystem = true),
            Category(id = teaCoffeeId, userId = userId, name = "Tea/Coffee", type = CategoryType.EXPENSE, icon = "☕", color = "#FF6B6B", parentId = drinksId, isSystem = true, quickAmounts = listOf(2000, 3000, 4000)),
            Category(id = juiceId, userId = userId, name = "Juice", type = CategoryType.EXPENSE, icon = "🧃", color = "#FF6B6B", parentId = drinksId, isSystem = true, quickAmounts = listOf(3000, 4000)),
            Category(id = snacksId, userId = userId, name = "Snacks", type = CategoryType.EXPENSE, icon = "🍿", color = "#FF6B6B", parentId = foodId, isSystem = true, quickAmounts = listOf(1000, 2000, 3000)),

            Category(id = transportId, userId = userId, name = "Transport", type = CategoryType.EXPENSE, icon = "🚌", color = "#4ECDC4", isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
            Category(id = autoId, userId = userId, name = "Auto", type = CategoryType.EXPENSE, icon = "🛺", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
            Category(id = busId, userId = userId, name = "Bus", type = CategoryType.EXPENSE, icon = "🚌", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(2000, 3000)),
            Category(id = cabId, userId = userId, name = "Cab", type = CategoryType.EXPENSE, icon = "🚕", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(8000, 12000, 16000)),

            Category(id = shoppingId, userId = userId, name = "Shopping", type = CategoryType.EXPENSE, icon = "🛍️", color = "#45B7D1", isSystem = true),
            Category(id = groceriesId, userId = userId, name = "Groceries", type = CategoryType.EXPENSE, icon = "🛒", color = "#45B7D1", parentId = shoppingId, isSystem = true, quickAmounts = listOf(20000, 50000, 100000)),
            Category(id = clothesId, userId = userId, name = "Clothes", type = CategoryType.EXPENSE, icon = "👕", color = "#45B7D1", parentId = shoppingId, isSystem = true, quickAmounts = listOf(50000, 100000, 200000)),

            Category(id = collegeId, userId = userId, name = "College", type = CategoryType.EXPENSE, icon = "📚", color = "#96CEB4", isSystem = true),
            Category(id = booksId, userId = userId, name = "Books", type = CategoryType.EXPENSE, icon = "📖", color = "#96CEB4", parentId = collegeId, isSystem = true, quickAmounts = listOf(20000, 50000)),
            Category(id = stationeryId, userId = userId, name = "Stationery", type = CategoryType.EXPENSE, icon = "✏️", color = "#96CEB4", parentId = collegeId, isSystem = true, quickAmounts = listOf(10000, 20000)),

            Category(id = entertainmentId, userId = userId, name = "Entertainment", type = CategoryType.EXPENSE, icon = "🎮", color = "#FFEAA7", isSystem = true),
            Category(id = moviesId, userId = userId, name = "Movies", type = CategoryType.EXPENSE, icon = "🎬", color = "#FFEAA7", parentId = entertainmentId, isSystem = true, quickAmounts = listOf(15000, 20000, 30000)),
            Category(id = gamesId, userId = userId, name = "Games", type = CategoryType.EXPENSE, icon = "🎮", color = "#FFEAA7", parentId = entertainmentId, isSystem = true, quickAmounts = listOf(10000, 20000)),

            Category(id = billsId, userId = userId, name = "Bills", type = CategoryType.EXPENSE, icon = "💡", color = "#DDA0DD", isSystem = true),
            Category(id = electricityId, userId = userId, name = "Electricity", type = CategoryType.EXPENSE, icon = "⚡", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(50000, 100000, 200000)),
            Category(id = internetId, userId = userId, name = "Internet", type = CategoryType.EXPENSE, icon = "🌐", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(50000, 100000)),
            Category(id = phoneId, userId = userId, name = "Phone", type = CategoryType.EXPENSE, icon = "📱", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(20000, 50000)),

            Category(id = healthId, userId = userId, name = "Health", type = CategoryType.EXPENSE, icon = "🏥", color = "#98FB98", isSystem = true),
            Category(id = medicineId, userId = userId, name = "Medicine", type = CategoryType.EXPENSE, icon = "💊", color = "#98FB98", parentId = healthId, isSystem = true, quickAmounts = listOf(10000, 20000, 50000)),
            Category(id = doctorId, userId = userId, name = "Doctor", type = CategoryType.EXPENSE, icon = "👨‍⚕️", color = "#98FB98", parentId = healthId, isSystem = true, quickAmounts = listOf(200000, 500000)),

            Category(id = subscriptionsId, userId = userId, name = "Subscriptions", type = CategoryType.EXPENSE, icon = "📱", color = "#F0E68C", isSystem = true),
            Category(id = netflixId, userId = userId, name = "Netflix", type = CategoryType.EXPENSE, icon = "🎬", color = "#F0E68C", parentId = subscriptionsId, isSystem = true, quickAmounts = listOf(20000, 65000)),
            Category(id = spotifyId, userId = userId, name = "Spotify", type = CategoryType.EXPENSE, icon = "🎵", color = "#F0E68C", parentId = subscriptionsId, isSystem = true, quickAmounts = listOf(10000, 15000)),

            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Salary", type = CategoryType.INCOME, icon = "💰", color = "#00C896", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Freelance", type = CategoryType.INCOME, icon = "💻", color = "#3A8EFF", isSystem = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Other", type = CategoryType.BOTH, icon = "📦", color = "#888888", isSystem = true)
        )
        defaults.forEach { addCategory(it) }
    }

    override suspend fun getCategoryTree(): List<Category> {
        val allCategories = db.categoryDao().getAllCategories().first()
        return buildTree(allCategories, null, 0)
    }

    private fun buildTree(allEntities: List<CategoryEntity>, parentId: String?, depth: Int): List<Category> {
        if (depth >= 3) return emptyList()
        return allEntities
            .filter { it.parentId == parentId }
            .sortedBy { it.sortOrder }
            .map { entity ->
                val children = buildTree(allEntities, entity.id, depth + 1)
                entity.toModel().copy(children = children)
            }
    }

    override suspend fun getRecentCategories(limit: Int): List<Category> {
        return db.categoryDao().getRecentCategories(limit).map { it.toModel() }
    }

    override suspend fun getMonthlySpendByCategory(): Map<String, Long> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis.toString()
        cal.add(java.util.Calendar.MONTH, 1)
        val endOfMonth = cal.timeInMillis.toString()
        return db.categoryDao().getMonthlySpendByCategory(startOfMonth, endOfMonth)
            .associate { it.id to it.total }
    }

    override suspend fun getChildrenOf(parentId: String): List<Category> {
        return db.categoryDao().getChildrenOf(parentId).map { it.toModel() }
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
