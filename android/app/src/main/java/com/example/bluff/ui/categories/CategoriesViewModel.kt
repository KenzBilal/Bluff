package com.example.bluff.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.domain.usecase.category.AddCategoryUseCase
import com.example.bluff.domain.usecase.category.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {
    val categories = getCategoriesUseCase.getAll()

    private val _categoryTree = MutableStateFlow<List<Category>>(emptyList())
    val categoryTree: StateFlow<List<Category>> = _categoryTree.asStateFlow()

    init {
        loadCategoryTree()
    }

    fun loadCategoryTree() {
        viewModelScope.launch {
            _categoryTree.value = getCategoriesUseCase.getCategoryTree()
        }
    }

    fun saveCategory(
        name: String,
        type: CategoryType,
        icon: String,
        color: String,
        existingId: String? = null,
        parentId: String? = null
    ) {
        viewModelScope.launch {
            val category = Category(
                id = existingId ?: "",
                userId = "",
                name = name,
                type = type,
                icon = icon,
                color = color,
                parentId = parentId
            )
            addCategoryUseCase(category)
            loadCategoryTree()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                CategoriesViewModel(
                    container.getCategoriesUseCase,
                    container.addCategoryUseCase
                )
            }
        }
    }
}
