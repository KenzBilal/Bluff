package com.example.bluff.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bluff.data.local.dao.*
import com.example.bluff.data.local.entity.*

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        RecurringTransactionEntity::class,
        TagEntity::class,
        TransactionTagEntity::class,
        AppSettingsEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BluffDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun tagDao(): TagDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        fun create(context: Context): BluffDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BluffDatabase::class.java,
                "bluff_db"
            ).build()
        }
    }
}
