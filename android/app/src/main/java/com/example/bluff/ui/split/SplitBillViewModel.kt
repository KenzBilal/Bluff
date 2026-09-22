package com.example.bluff.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.SplitBill
import com.example.bluff.domain.model.SplitParticipant
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.category.GetCategoriesUseCase
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import com.example.bluff.data.repository.SplitBillRepository
import com.example.bluff.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class SplitMode { EQUAL, CUSTOM }

data class EditableParticipant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String? = null,
    val amountMinor: Long = 0L,
    val isMe: Boolean = false
)

class SplitBillViewModel(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val splitBillRepository: SplitBillRepository,
    private val userPrefs: UserPreferencesManager
) : ViewModel() {

    val accounts = getAccountsUseCase.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = getCategoriesUseCase.getExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalAmountText = MutableStateFlow("")
    val totalAmountText: StateFlow<String> = _totalAmountText.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _splitMode = MutableStateFlow(SplitMode.EQUAL)
    val splitMode: StateFlow<SplitMode> = _splitMode.asStateFlow()

    private val _participants = MutableStateFlow<List<EditableParticipant>>(
        listOf(EditableParticipant(name = "Me", isMe = true))
    )
    val participants: StateFlow<List<EditableParticipant>> = _participants.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<Unit>?>(null)
    val saveResult: StateFlow<Result<Unit>?> = _saveResult.asStateFlow()

    fun setTotalAmount(text: String) {
        val sanitized = text.filter { it.isDigit() }
        _totalAmountText.value = sanitized
        recalculateEqualSplit(sanitized)
    }

    fun setAccountId(id: String) { _selectedAccountId.value = id }
    fun setCategoryId(id: String) { _selectedCategoryId.value = id }
    
    fun setSplitMode(mode: SplitMode) {
        _splitMode.value = mode
        if (mode == SplitMode.EQUAL) {
            recalculateEqualSplit(_totalAmountText.value)
        }
    }

    fun addContact(name: String, phone: String? = null) {
        val current = _participants.value.toMutableList()
        current.add(EditableParticipant(name = name, phone = phone))
        _participants.value = current
        if (_splitMode.value == SplitMode.EQUAL) recalculateEqualSplit(_totalAmountText.value)
    }

    fun removeParticipant(id: String) {
        val current = _participants.value.toMutableList()
        current.removeAll { it.id == id && !it.isMe }
        _participants.value = current
        if (_splitMode.value == SplitMode.EQUAL) recalculateEqualSplit(_totalAmountText.value)
    }

    fun updateParticipantCustomAmount(id: String, amountMinor: Long) {
        if (_splitMode.value == SplitMode.EQUAL) return
        val current = _participants.value.map {
            if (it.id == id) it.copy(amountMinor = amountMinor) else it
        }
        _participants.value = current
    }

    private fun recalculateEqualSplit(textAmount: String) {
        val amt = (textAmount.toLongOrNull() ?: 0L) * 100L
        val parts = _participants.value
        if (parts.isEmpty()) return

        val equalShare = amt / parts.size
        val remainder = (amt % parts.size).toInt() // in paise, max = participants-1

        // Distribute remainder 1 paisa at a time to first N participants (not Me)
        val friends = parts.filter { !it.isMe }
        val me = parts.firstOrNull { it.isMe }

        var remainderLeft = remainder
        val updated = parts.map { p ->
            when {
                p.isMe -> p.copy(amountMinor = equalShare)
                remainderLeft > 0 -> {
                    remainderLeft--
                    p.copy(amountMinor = equalShare + 1L)
                }
                else -> p.copy(amountMinor = equalShare)
            }
        }

        // If no friends to absorb remainder, give it to Me
        if (remainder > 0 && friends.isEmpty() && me != null) {
            _participants.value = updated.map {
                if (it.isMe) it.copy(amountMinor = equalShare + remainder) else it
            }
        } else {
            _participants.value = updated
        }
    }

    fun saveSplit() {
        viewModelScope.launch {
            try {
                val totalMinor = _totalAmountText.value.toLongOrNull()?.times(100) ?: 0L
                if (totalMinor <= 0) throw Exception("Enter a valid amount")
                val accId = _selectedAccountId.value ?: throw Exception("Select an account")
                val catId = _selectedCategoryId.value ?: throw Exception("Select a category")
                
                val parts = _participants.value
                val sum = parts.sumOf { it.amountMinor }
                if (sum != totalMinor) throw Exception("Participant amounts must equal total bill")

                val userId = userPrefs.getUserIdBlocking()
                val transactionId = UUID.randomUUID().toString()

                // 1. Create main expense
                val expense = Transaction(
                    id = transactionId,
                    userId = userId,
                    accountId = accId,
                    categoryId = catId,
                    amountMinor = totalMinor,
                    type = TransactionType.EXPENSE,
                    note = "Split Bill",
                    transactionDate = LocalDate.now(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val txResult = addTransactionUseCase(expense)
                if (txResult.isFailure) throw Exception("Failed to save transaction")

                // 2. Create Split Records
                val splitBillId = UUID.randomUUID().toString()
                val splitParticipants = parts.filter { !it.isMe }.map {
                    SplitParticipant(
                        id = UUID.randomUUID().toString(),
                        splitBillId = splitBillId,
                        contactName = it.name,
                        contactPhone = it.phone,
                        amountMinor = it.amountMinor,
                        isPaid = false
                    )
                }
                
                val splitBill = SplitBill(
                    id = splitBillId,
                    transactionId = transactionId,
                    totalAmountMinor = totalMinor,
                    participants = splitParticipants
                )
                
                val splitResult = splitBillRepository.saveSplitBill(splitBill)
                if (splitResult.isFailure) throw Exception("Failed to save split details")

                _saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun consumeSaveResult() {
        _saveResult.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                SplitBillViewModel(
                    container.getAccountsUseCase,
                    container.getCategoriesUseCase,
                    container.addTransactionUseCase,
                    container.splitBillRepository,
                    container.userPreferencesManager
                )
            }
        }
    }
}
