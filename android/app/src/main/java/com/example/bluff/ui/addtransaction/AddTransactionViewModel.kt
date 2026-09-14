package com.example.bluff.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.category.GetCategoriesUseCase
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _amount = MutableStateFlow(0L)
    val amount: StateFlow<Long> = _amount.asStateFlow()

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type: StateFlow<TransactionType> = _type.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    private val _selectedToAccountId = MutableStateFlow<String?>(null)
    val selectedToAccountId: StateFlow<String?> = _selectedToAccountId.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    val accounts: StateFlow<List<Account>> = getAccountsUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenseCategories: StateFlow<List<Category>> = getCategoriesUseCase.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories: StateFlow<List<Category>> = getCategoriesUseCase.getIncomeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle)
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow()

    fun setType(newType: TransactionType) {
        _type.value = newType
        _selectedCategoryId.value = null
    }

    fun setNote(newNote: String) { _note.value = newNote }
    fun setAccountId(id: String) { _selectedAccountId.value = id }
    fun setToAccountId(id: String) { _selectedToAccountId.value = id }
    fun setCategoryId(id: String) { _selectedCategoryId.value = id }

    fun appendAmount(digit: Int) {
        _amount.value = (_amount.value * 10) + digit
    }

    fun removeAmount() {
        _amount.value = _amount.value / 10
    }

    fun save() {
        val currentAmount = _amount.value
        val currentType = _type.value
        val accountId = _selectedAccountId.value
        val categoryId = _selectedCategoryId.value
        val toAccountId = _selectedToAccountId.value

        if (currentAmount <= 0) {
            _saveResult.value = SaveResult.Error("Amount must be greater than zero")
            return
        }
        if (accountId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select an account")
            return
        }
        if (currentType != TransactionType.TRANSFER && categoryId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select a category")
            return
        }
        if (currentType == TransactionType.TRANSFER && toAccountId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select destination account")
            return
        }
        if (currentType == TransactionType.TRANSFER && accountId == toAccountId) {
            _saveResult.value = SaveResult.Error("Source and destination must differ")
            return
        }

        viewModelScope.launch {
            val transaction = Transaction(
                id = "",
                userId = "",
                amountMinor = currentAmount,
                type = currentType,
                accountId = accountId,
                toAccountId = if (currentType == TransactionType.TRANSFER) toAccountId else null,
                categoryId = if (currentType != TransactionType.TRANSFER) categoryId else null,
                note = _note.value.ifBlank { null },
                transactionDate = LocalDate.now()
            )
            val result = addTransactionUseCase(transaction)
            result.fold(
                onSuccess = {
                    _saveResult.value = SaveResult.Success
                    resetForm()
                },
                onFailure = { e ->
                    _saveResult.value = SaveResult.Error(e.message ?: "Failed to save")
                }
            )
        }
    }

    fun resetForm() {
        _amount.value = 0L
        _type.value = TransactionType.EXPENSE
        _note.value = ""
        _selectedCategoryId.value = null
        // Keep account selection
    }

    fun consumeSaveResult() {
        _saveResult.value = SaveResult.Idle
    }

    sealed class SaveResult {
        data object Idle : SaveResult()
        data object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                AddTransactionViewModel(
                    container.addTransactionUseCase,
                    container.getAccountsUseCase,
                    container.getCategoriesUseCase
                )
            }
        }
    }
}
