package com.example.bluff.domain.usecase.account

import com.example.bluff.data.repository.AccountRepository
import com.example.bluff.domain.model.Account
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val repository: AccountRepository) {
    fun getAll(): Flow<List<Account>> = repository.getAllAccounts()
    fun getActive(): Flow<List<Account>> = repository.getAllAccounts()
    fun getById(id: String): Flow<Account?> = repository.getAccountById(id)
}
