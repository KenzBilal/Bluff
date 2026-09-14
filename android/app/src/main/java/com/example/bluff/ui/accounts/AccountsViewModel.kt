package com.example.bluff.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.AccountType
import com.example.bluff.domain.usecase.account.AddAccountUseCase
import com.example.bluff.domain.usecase.account.DeleteAccountUseCase
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.account.UpdateAccountUseCase

import kotlinx.coroutines.launch

class AccountsViewModel(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    val accounts = getAccountsUseCase.getActive()

    fun saveAccount(
        name: String,
        type: AccountType,
        initialBalance: Long,
        icon: String,
        color: String,
        existingId: String? = null
    ) {
        viewModelScope.launch {
            val account = Account(
                id = existingId ?: "",
                userId = "",
                name = name,
                type = type,
                initialBalanceMinor = initialBalance,
                icon = icon,
                color = color
            )
            if (existingId != null) {
                updateAccountUseCase(account)
            } else {
                addAccountUseCase(account)
            }
        }
    }

    fun archiveAccount(id: String) {
        viewModelScope.launch {
            deleteAccountUseCase(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                AccountsViewModel(
                    container.getAccountsUseCase,
                    container.addAccountUseCase,
                    container.updateAccountUseCase,
                    container.deleteAccountUseCase
                )
            }
        }
    }
}
