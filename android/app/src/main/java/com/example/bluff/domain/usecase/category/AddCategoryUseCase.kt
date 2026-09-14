package com.example.bluff.domain.usecase.category

import com.example.bluff.data.repository.CategoryRepository
import com.example.bluff.domain.model.Category

class AddCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): Result<String> {
        if (category.name.isBlank()) return Result.failure(IllegalArgumentException("Category name cannot be empty"))
        return repository.addCategory(category)
    }
}
