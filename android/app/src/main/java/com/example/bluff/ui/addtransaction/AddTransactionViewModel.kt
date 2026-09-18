package com.example.bluff.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.Debt
import com.example.bluff.domain.model.DebtDirection
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.model.RecurringTransaction
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.category.GetCategoriesUseCase
import com.example.bluff.domain.usecase.category.QuickSuggestions
import com.example.bluff.domain.usecase.cycle.AddCycleUseCase
import com.example.bluff.domain.usecase.debt.AddDebtUseCase
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import com.example.bluff.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.example.bluff.domain.usecase.recurring.PayRecurringTransactionUseCase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** The mode of the add-transaction sheet. Debt replaces Income. */
enum class EntryMode { EXPENSE, DEBT, TRANSFER }

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val addCycleUseCase: AddCycleUseCase,
    private val getRecurringTransactionsUseCase: GetRecurringTransactionsUseCase,
    private val payRecurringTransactionUseCase: PayRecurringTransactionUseCase
) : ViewModel() {

    // ── Amount ──────────────────────────────────────────────────────────────
    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _amount = MutableStateFlow(0L)
    val amount: StateFlow<Long> = _amount.asStateFlow()

    // ── Entry mode ──────────────────────────────────────────────────────────
    private val _mode = MutableStateFlow(EntryMode.EXPENSE)
    val mode: StateFlow<EntryMode> = _mode.asStateFlow()

    // ── Transfer / Expense fields ────────────────────────────────────────────
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    private val _selectedToAccountId = MutableStateFlow<String?>(null)
    val selectedToAccountId: StateFlow<String?> = _selectedToAccountId.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    // ── Transfer: optional contact (person you transferred to) ───────────────
    private val _transferContactName = MutableStateFlow("")
    val transferContactName: StateFlow<String> = _transferContactName.asStateFlow()

    private val _transferContactPhone = MutableStateFlow("")
    val transferContactPhone: StateFlow<String> = _transferContactPhone.asStateFlow()

    // ── Debt fields ─────────────────────────────────────────────────────────
    private val _debtDirection = MutableStateFlow(DebtDirection.THEY_OWE)
    val debtDirection: StateFlow<DebtDirection> = _debtDirection.asStateFlow()

    private val _debtContactName = MutableStateFlow("")
    val debtContactName: StateFlow<String> = _debtContactName.asStateFlow()

    private val _debtContactPhone = MutableStateFlow("")
    val debtContactPhone: StateFlow<String> = _debtContactPhone.asStateFlow()

    // ── Cycle default ─────────────────────────────────────────────────────
    private val _defaultCycleDays = MutableStateFlow<Int?>(null)
    val defaultCycleDays: StateFlow<Int?> = _defaultCycleDays.asStateFlow()

    // ── Repos ────────────────────────────────────────────────────────────────
    val accounts: StateFlow<List<Account>> = getAccountsUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> = getCategoriesUseCase.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle)
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow()

    private val _categoryTree = MutableStateFlow<List<Category>>(emptyList())
    val categoryTree: StateFlow<List<Category>> = _categoryTree.asStateFlow()

    private val _quickSuggestions = MutableStateFlow<QuickSuggestions?>(null)
    val quickSuggestions: StateFlow<QuickSuggestions?> = _quickSuggestions.asStateFlow()

    private val _monthlySpend = MutableStateFlow<Map<String, Long>>(emptyMap())
    val monthlySpend: StateFlow<Map<String, Long>> = _monthlySpend.asStateFlow()

    val dueRecurringTransactions: StateFlow<List<RecurringTransaction>> = getRecurringTransactionsUseCase.getActive()
        .map { recurring -> 
            val now = LocalDate.now()
            recurring.filter { !it.nextRunDate.isAfter(now) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRecurringId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            _categoryTree.value = getCategoriesUseCase.getCategoryTree()
            val suggestions = getCategoriesUseCase.getQuickSuggestions()
            _quickSuggestions.value = suggestions
            _monthlySpend.value = suggestions.monthlySpend
        }
        viewModelScope.launch {
            getAccountsUseCase.getActive().collect { accounts ->
                if (_selectedAccountId.value == null && accounts.isNotEmpty()) {
                    _selectedAccountId.value = accounts.first().id
                }
            }
        }
    }

    fun setMode(newMode: EntryMode) {
        _mode.value = newMode
        _selectedCategoryId.value = null
    }

    fun setNote(newNote: String) { _note.value = newNote }
    fun setAccountId(id: String) { _selectedAccountId.value = id }
    fun setToAccountId(id: String) { _selectedToAccountId.value = id }
    fun setCategoryId(id: String) {
        _selectedCategoryId.value = id
        viewModelScope.launch {
            val default = AppContainer.instance.expenseCycleRepository.getDefaultForCategory(id)
            _defaultCycleDays.value = default?.defaultCycleDays
        }
    }

    fun createCycle(categoryId: String, name: String, amountMinor: Long, cycleDays: Int) {
        viewModelScope.launch {
            addCycleUseCase(categoryId, name, amountMinor, cycleDays)
        }
    }

    fun reset() {
        _amountText.value = ""
        _amount.value = 0L
        _mode.value = EntryMode.EXPENSE
        _note.value = ""
        _selectedToAccountId.value = null
        _selectedCategoryId.value = null
        _debtDirection.value = DebtDirection.THEY_OWE
        _debtContactName.value = ""
        _transferContactName.value = ""
        _saveResult.value = SaveResult.Idle
        _defaultCycleDays.value = null
    }

    fun setDebtDirection(dir: DebtDirection) { _debtDirection.value = dir }
    fun setDebtContact(name: String, phone: String) {
        _debtContactName.value = name
        _debtContactPhone.value = phone
    }
    fun setTransferContact(name: String, phone: String) {
        _transferContactName.value = name
        _transferContactPhone.value = phone
    }

    fun setAmountText(text: String) {
        val digits = text.filter { it.isDigit() }
        _amountText.value = digits
        _amount.value = digits.toLongOrNull()?.times(100L) ?: 0L
    }

    fun applyRecurringSuggestion(recurring: RecurringTransaction) {
        _amount.value = recurring.amountMinor
        _amountText.value = (recurring.amountMinor / 100).toString()
        _note.value = recurring.name
        _selectedAccountId.value = recurring.accountId
        _selectedCategoryId.value = recurring.categoryId
        _mode.value = EntryMode.EXPENSE
        _selectedRecurringId.value = recurring.id
    }

    fun save() {
        val currentAmount = _amount.value
        val accountId = _selectedAccountId.value
        val currentMode = _mode.value

        if (currentAmount <= 0) {
            _saveResult.value = SaveResult.Error("Amount must be greater than zero")
            return
        }

        when (currentMode) {
            EntryMode.DEBT -> saveDebt(currentAmount)
            EntryMode.EXPENSE -> saveExpense(currentAmount, accountId)
            EntryMode.TRANSFER -> saveTransfer(currentAmount, accountId)
        }
    }

    private fun saveDebt(amount: Long) {
        val contactName = _debtContactName.value.trim()
        if (contactName.isBlank()) {
            _saveResult.value = SaveResult.Error("Select a contact")
            return
        }
        viewModelScope.launch {
            val debt = Debt(
                id = "",
                userId = "",
                amountMinor = amount,
                direction = _debtDirection.value,
                contactName = contactName,
                contactPhone = _debtContactPhone.value.ifBlank { null },
                note = _note.value.ifBlank { null }
            )
            addDebtUseCase(debt).fold(
                onSuccess = { _saveResult.value = SaveResult.Success; resetForm() },
                onFailure = { e -> _saveResult.value = SaveResult.Error(e.message ?: "Failed to save") }
            )
        }
    }

    private fun saveExpense(amount: Long, accountId: String?) {
        val categoryId = _selectedCategoryId.value
        if (accountId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select an account")
            return
        }
        if (categoryId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select a category")
            return
        }
        viewModelScope.launch {
            val recurringId = _selectedRecurringId.value
            val due = dueRecurringTransactions.value.find { it.id == recurringId }
            if (due != null) {
                payRecurringTransactionUseCase(due).fold(
                    onSuccess = { _saveResult.value = SaveResult.Success; resetForm() },
                    onFailure = { e -> _saveResult.value = SaveResult.Error(e.message ?: "Failed to save") }
                )
            } else {
                val transaction = Transaction(
                    id = "", userId = "",
                    amountMinor = amount,
                    type = TransactionType.EXPENSE,
                    accountId = accountId,
                    categoryId = categoryId,
                    note = _note.value.ifBlank { null },
                    transactionDate = LocalDate.now()
                )
                addTransactionUseCase(transaction).fold(
                    onSuccess = { _saveResult.value = SaveResult.Success; resetForm() },
                    onFailure = { e -> _saveResult.value = SaveResult.Error(e.message ?: "Failed to save") }
                )
            }
        }
    }

    private fun saveTransfer(amount: Long, accountId: String?) {
        val toAccountId = _selectedToAccountId.value
        if (accountId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select source account")
            return
        }
        if (toAccountId.isNullOrBlank()) {
            _saveResult.value = SaveResult.Error("Select destination account")
            return
        }
        if (accountId == toAccountId) {
            _saveResult.value = SaveResult.Error("Source and destination must differ")
            return
        }
        val contactNote = _transferContactName.value.trim()
            .let { if (it.isNotBlank()) "To: $it. " else "" }
        viewModelScope.launch {
            val transaction = Transaction(
                id = "", userId = "",
                amountMinor = amount,
                type = TransactionType.TRANSFER,
                accountId = accountId,
                toAccountId = toAccountId,
                note = contactNote + _note.value.ifBlank { "" }.trim(),
                transactionDate = LocalDate.now()
            )
            addTransactionUseCase(transaction).fold(
                onSuccess = { _saveResult.value = SaveResult.Success; resetForm() },
                onFailure = { e -> _saveResult.value = SaveResult.Error(e.message ?: "Failed to save") }
            )
        }
    }

    fun resetForm() {
        _amountText.value = ""
        _amount.value = 0L
        _mode.value = EntryMode.EXPENSE
        _note.value = ""
        _selectedCategoryId.value = null
        _debtContactName.value = ""
        _debtContactPhone.value = ""
        _transferContactName.value = ""
        _transferContactPhone.value = ""
        _debtDirection.value = DebtDirection.THEY_OWE
        _selectedRecurringId.value = null
    }

    fun consumeSaveResult() { _saveResult.value = SaveResult.Idle }

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
                    container.getCategoriesUseCase,
                    container.addDebtUseCase,
                    container.addCycleUseCase,
                    container.getRecurringTransactionsUseCase,
                    container.payRecurringTransactionUseCase
                )
            }
        }
    }
}
