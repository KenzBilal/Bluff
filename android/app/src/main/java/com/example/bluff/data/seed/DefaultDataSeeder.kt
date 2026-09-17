package com.example.bluff.data.seed

import com.example.bluff.data.repository.AccountRepository
import com.example.bluff.data.repository.CategoryRepository
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.AccountType
import kotlinx.coroutines.flow.first
import java.util.UUID

class DefaultDataSeeder(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) {
    suspend fun seedIfNeeded() {
        seedCategoriesIfNeeded()
        seedAccountsIfNeeded()
    }

    private suspend fun seedCategoriesIfNeeded() {
        categoryRepository.seedDefaultCategoriesIfNeeded()
    }

    private suspend fun seedAccountsIfNeeded() {
        val accounts = accountRepository.getAllAccounts().first()
        if (accounts.isNotEmpty()) return

        // Seed UPI account (default)
        val upiId = UUID.randomUUID().toString()
        accountRepository.addAccount(
            Account(
                id = upiId,
                userId = "",
                name = "UPI",
                type = AccountType.BANK, // Using BANK type for UPI
                icon = "phone_iphone",
                color = "#3A8EFF",
                initialBalanceMinor = 0L
            )
        )

        // Seed Cash account
        accountRepository.addAccount(
            Account(
                id = UUID.randomUUID().toString(),
                userId = "",
                name = "Cash",
                type = AccountType.CASH,
                icon = "payments",
                color = "#00C896",
                initialBalanceMinor = 0L
            )
        )
    }
}
