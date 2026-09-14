package com.example.bluff.domain.usecase.account

import com.example.bluff.data.repository.AccountRepository
import com.example.bluff.domain.model.Account

class AddAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account): Result<String> {
        if (account.name.isBlank()) return Result.failure(IllegalArgumentException("Account name cannot be empty"))
        return repository.addAccount(account)
    }
}
