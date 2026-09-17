package com.example.bluff.domain.usecase.category

import com.example.bluff.data.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(categoryId: String): Result<Unit> {
        return repository.deleteCategory(categoryId)
    }
}
