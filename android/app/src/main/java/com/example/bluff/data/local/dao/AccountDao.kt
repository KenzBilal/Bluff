package com.example.bluff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bluff.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isArchived = 1 WHERE id = :id")
    suspend fun archiveAccount(id: String)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()

    /**
     * Calculate real-time balance for an account:
     * initialBalance + SUM(income) + SUM(incoming transfers) - SUM(expenses) - SUM(outgoing transfers)
     */
    @Query("""
        SELECT 
            a.initialBalance + 
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) +
            COALESCE(SUM(CASE WHEN t.type = 'TRANSFER' AND t.toAccountId = :id THEN t.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN t.type = 'TRANSFER' AND t.accountId = :id THEN t.amount ELSE 0 END), 0)
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id OR t.toAccountId = a.id
        WHERE a.id = :id
    """)
    fun getAccountBalance(id: String): Flow<Long>

    @Query("""
        SELECT 
            SUM(a.initialBalance) +
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.isArchived = 0
    """)
    fun getTotalBalance(): Flow<Long>
}
