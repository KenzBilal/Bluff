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
        DebtEntity::class,
        ExpenseCycleEntity::class,
        CategoryCycleDefaultEntity::class
    ],
    version = 6,
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
    abstract fun debtDao(): DebtDao
    abstract fun expenseCycleDao(): ExpenseCycleDao
    abstract fun categoryCycleDefaultDao(): CategoryCycleDefaultDao

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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS sync_queue")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN goalId TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE expense_cycles (
                        id TEXT PRIMARY KEY NOT NULL,
                        categoryId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL DEFAULT 0,
                        cycleDays INTEGER NOT NULL,
                        lastTransactionDate TEXT NOT NULL,
                        nextDueDate TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX index_expense_cycles_categoryId ON expense_cycles(categoryId)")
                db.execSQL("""
                    CREATE TABLE category_cycle_defaults (
                        categoryId TEXT PRIMARY KEY NOT NULL,
                        categoryName TEXT NOT NULL,
                        defaultCycleDays INTEGER NOT NULL
                    )
                """)
            }
        }

        fun create(context: Context): BluffDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BluffDatabase::class.java,
                "bluff_db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
        }
    }
}

