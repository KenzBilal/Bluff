package com.example.bluff.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.TransactionEntity
import com.example.bluff.data.preferences.UserPreferencesManager
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.util.RecurrenceUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class RecurringWorker(
    context: Context,
    params: WorkerParameters,
    private val db: BluffDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val userId = UserPreferencesManager(applicationContext).getUserIdBlocking()

        val dueTransactions = db.recurringTransactionDao().getDueRecurringTransactions(todayStr)

        for (recurring in dueTransactions) {
            if (recurring.endDate != null) {
                val endDate = LocalDate.parse(recurring.endDate)
                if (today.isAfter(endDate)) {
                    db.recurringTransactionDao().deactivate(recurring.id)
                    continue
                }
            }

            val transactionEntity = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = recurring.accountId,
                categoryId = recurring.categoryId,
                amount = recurring.amount,
                type = recurring.type,
                note = recurring.name,
                transactionDate = todayStr,
                recurringId = recurring.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.transactionDao().insertTransaction(transactionEntity)

            val frequency = RecurrenceFrequency.valueOf(recurring.frequency)
            val nextDate = RecurrenceUtil.calculateNextRunDate(today, frequency)
            db.recurringTransactionDao().advanceNextRunDate(recurring.id, nextDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        return Result.success()
    }
}
