package com.example.bluff.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _amount = MutableStateFlow(0L)
    val amount: StateFlow<Long> = _amount

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type: StateFlow<TransactionType> = _type

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    fun setType(newType: TransactionType) { _type.value = newType }
    fun setNote(newNote: String) { _note.value = newNote }
    
    fun appendAmount(digit: Int) {
        _amount.value = (_amount.value * 10) + digit
    }
    
    fun removeAmount() {
        _amount.value = _amount.value / 10
    }

    fun save() {
        viewModelScope.launch {
            // appContainer.addTransactionUseCase(...)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddTransactionViewModel(AppContainer.instance)
            }
        }
    }
}
