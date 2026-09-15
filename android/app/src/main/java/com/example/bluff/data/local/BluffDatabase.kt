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
        SyncQueueEntity::class
    ],
    version = 2,
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
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN parentId TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN quickAmounts TEXT DEFAULT '[]'")
            }
        }

        fun create(context: Context): BluffDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BluffDatabase::class.java,
                "bluff_db"
            ).addMigrations(MIGRATION_1_2).build()
        }
    }
}
