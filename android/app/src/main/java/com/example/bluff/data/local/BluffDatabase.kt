package com.example.bluff.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        SyncQueueEntity::class,
        DebtEntity::class
    ],
    version = 3,
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
    abstract fun debtDao(): DebtDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN parentId TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN quickAmounts TEXT DEFAULT '[]'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS debts (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        direction TEXT NOT NULL,
                        contactName TEXT NOT NULL,
                        contactPhone TEXT,
                        note TEXT,
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        paidAt INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        fun create(context: Context): BluffDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BluffDatabase::class.java,
                "bluff_db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
        }
    }
}

