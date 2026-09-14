package com.example.bluff.domain.usecase.category

import com.example.bluff.data.repository.CategoryRepository
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    fun getAll(): Flow<List<Category>> = repository.getAllCategories()
    fun getByType(type: CategoryType): Flow<List<Category>> = repository.getCategoriesByType(type)
    fun getExpenseCategories(): Flow<List<Category>> = repository.getExpenseCategories()
    fun getIncomeCategories(): Flow<List<Category>> = repository.getIncomeCategories()
}
