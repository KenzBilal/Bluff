package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.AccountEntity
import com.example.bluff.data.local.entity.SyncQueueEntity
import com.example.bluff.data.remote.dto.AccountDto
import com.example.bluff.domain.model.Account
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    private val supabase: SupabaseClient,
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
            queueSync("INSERT", "accounts", id, AccountDto.fromEntity(entity))
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAccount(account: Account): Result<Unit> {
        return try {
            val entity = AccountEntity.fromModel(account)
            db.accountDao().updateAccount(entity)
            queueSync("UPDATE", "accounts", account.id, AccountDto.fromEntity(entity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun archiveAccount(id: String): Result<Unit> {
        return try {
            db.accountDao().archiveAccount(id)
            queueSync("UPDATE_ARCHIVE", "accounts", id, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queueSync(op: String, table: String, id: String, payload: AccountDto?) {
        db.syncQueueDao().insertOperation(
            SyncQueueEntity(
                operationType = op,
                tableName = table,
                entityId = id,
                payload = payload?.let { Json.encodeToString(it) },
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
