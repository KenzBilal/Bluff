package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.AccountEntity
import com.example.bluff.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAccountById(id: String): Flow<Account?>
    suspend fun addAccount(account: Account): Result<String>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun archiveAccount(id: String): Result<Unit>
}

class AccountRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> =
        db.accountDao().getAllAccounts().map { entities -> entities.map { it.toModel() } }

    override fun getActiveAccounts(): Flow<List<Account>> =
        db.accountDao().getActiveAccounts().map { entities -> entities.map { it.toModel() } }

    override fun getAccountById(id: String): Flow<Account?> =
        db.accountDao().getAccountBalance(id).map { balance ->
            db.accountDao().getAccountById(id)?.toModel(balance)
        }

    override suspend fun addAccount(account: Account): Result<String> {
        return try {
            val id = if (account.id.isEmpty()) UUID.randomUUID().toString() else account.id
            val userId = userIdProvider()
            val entity = AccountEntity.fromModel(account.copy(id = id, userId = userId))
            db.accountDao().insertAccount(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(account: Account): Result<Unit> {
        return try {
            val entity = AccountEntity.fromModel(account)
            db.accountDao().updateAccount(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun archiveAccount(id: String): Result<Unit> {
        return try {
            db.accountDao().archiveAccount(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
