package com.example.bluff.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AccountDetailViewModel(
    accountId: String,
    getAccountsUseCase: GetAccountsUseCase,
    getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    val account: StateFlow<Account?> = getAccountsUseCase.getById(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<Transaction>> = getTransactionsUseCase.getByAccount(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(accountId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                AccountDetailViewModel(
                    accountId = accountId,
                    getAccountsUseCase = container.getAccountsUseCase,
                    getTransactionsUseCase = container.getTransactionsUseCase
                )
            }
        }
    }
}
