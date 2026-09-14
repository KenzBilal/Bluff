package com.example.bluff.domain.usecase.account

import com.example.bluff.data.repository.AccountRepository

class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accountId: String): Result<Unit> =
        repository.archiveAccount(accountId)
}
