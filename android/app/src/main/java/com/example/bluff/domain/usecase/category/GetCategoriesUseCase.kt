package com.example.bluff.domain.usecase.category

import com.example.bluff.data.repository.CategoryRepository
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

data class QuickSuggestions(
    val categories: List<Category>,
    val monthlySpend: Map<String, Long>
)

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    fun getAll(): Flow<List<Category>> = repository.getAllCategories()
    fun getByType(type: CategoryType): Flow<List<Category>> = repository.getCategoriesByType(type)
    fun getExpenseCategories(): Flow<List<Category>> = repository.getExpenseCategories()
    fun getIncomeCategories(): Flow<List<Category>> = repository.getIncomeCategories()

    suspend fun getMonthlySpendByCategory(): Map<String, Long> = repository.getMonthlySpendByCategory()

    suspend fun getCategoryTree(): List<Category> {
        return repository.getCategoryTree()
    }

    suspend fun getQuickSuggestions(): QuickSuggestions {
        val recentCategories = repository.getRecentCategories(5)
        val monthlySpend = repository.getMonthlySpendByCategory()
        val hour = LocalTime.now().hour

        val timeSuggestedNames = when (hour) {
            in 6..10 -> listOf("Breakfast", "Tea/Coffee")
            in 11..15 -> listOf("Lunch")
            in 16..20 -> listOf("Dinner", "Snacks")
            else -> listOf("Entertainment")
        }

        val allCategories = repository.getCategoryTree()
        val flatCategories = allCategories.flatMap { root ->
            listOf(root) + root.children.flatMap { level1 ->
                listOf(level1) + level1.children
            }
        }

        val timeBased = flatCategories.filter { cat ->
            timeSuggestedNames.any { name ->
                cat.name.contains(name, ignoreCase = true)
            }
        }.take(3)

        val seen = mutableSetOf<String>()
        val suggestions = mutableListOf<Category>()
        for (cat in timeBased + recentCategories) {
            if (cat.id !in seen && cat.parentId != null) {
                seen.add(cat.id)
                suggestions.add(cat)
            }
        }

        return QuickSuggestions(
            categories = suggestions.take(6),
            monthlySpend = monthlySpend
        )
    }
}
