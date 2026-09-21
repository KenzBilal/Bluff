package com.example.bluff.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.bluff.MainActivity
import com.example.bluff.R
import com.example.bluff.data.local.entity.TransactionEntity
import com.example.bluff.data.preferences.UserPreferencesManager
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == "com.example.bluff.TEST_SMS") {
                    val testSender = intent.getStringExtra("TEST_SENDER") ?: ""
                    val testBody = intent.getStringExtra("TEST_BODY") ?: ""
                    parseAndInsertTransaction(context, testSender, testBody)
                    return@launch
                }

                if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return@launch

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return@launch

                val fullMessage = StringBuilder()
                var sender = ""

                for (sms in messages) {
                    fullMessage.append(sms.displayMessageBody)
                    sender = sms.displayOriginatingAddress ?: ""
                }

                val messageBody = fullMessage.toString().replace("\n", " ")

                if (sender.contains("SBI", ignoreCase = true) || sender.contains("KOTAK", ignoreCase = true)) {
                    parseAndInsertTransaction(context, sender, messageBody)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun parseAndInsertTransaction(context: Context, sender: String, messageBody: String) {
        // Initialize AppContainer if needed
        try {
            AppContainer.instance
        } catch (e: Exception) {
            AppContainer.init(context.applicationContext)
        }

        var amountMinor: Long? = null
        var merchant = "Unknown"
        var isExpense = false

        if (sender.contains("SBI", ignoreCase = true)) {
            val sbiDebitRegex = Regex("debited by (\\d+(?:\\.\\d+)?).*?trf to (.*?)\\s+Refno", RegexOption.IGNORE_CASE)
            val match = sbiDebitRegex.find(messageBody)
            if (match != null) {
                val amountStr = match.groupValues[1]
                merchant = match.groupValues[2].trim()
                amountMinor = amountStr.toDoubleOrNull()?.times(100)?.toLong()
                isExpense = true
            }
        } else if (sender.contains("KOTAK", ignoreCase = true)) {
            val kotakSentRegex = Regex("Sent Rs\\.?(\\d+(?:\\.\\d+)?).*? to (.*?)\\s+on", RegexOption.IGNORE_CASE)
            val kotakReceivedRegex = Regex("Received Rs\\.?(\\d+(?:\\.\\d+)?) from (.*?) in your", RegexOption.IGNORE_CASE)

            var match = kotakSentRegex.find(messageBody)
            if (match != null) {
                val amountStr = match.groupValues[1]
                amountMinor = amountStr.toDoubleOrNull()?.times(100)?.toLong()
                merchant = match.groupValues[2].trim()
                isExpense = true
            } else {
                match = kotakReceivedRegex.find(messageBody)
                if (match != null) {
                    val amountStr = match.groupValues[1]
                    amountMinor = amountStr.toDoubleOrNull()?.times(100)?.toLong()
                    merchant = match.groupValues[2].trim()
                    isExpense = false
                }
            }
        }

        if (amountMinor != null && amountMinor > 0) {
            val db = AppContainer.instance.db
            val userId = UserPreferencesManager(context.applicationContext).getUserIdBlocking()
            
            // 1. Account Mapping
            val accounts = db.accountDao().getAllAccounts().first()
            var accountId = accounts.firstOrNull()?.id ?: return
            
            val bankName = when {
                sender.contains("SBI", ignoreCase = true) -> "SBI"
                sender.contains("KOTAK", ignoreCase = true) -> "KOTAK"
                else -> ""
            }
            
            if (bankName.isNotEmpty()) {
                val matchedAccount = accounts.firstOrNull { it.name.contains(bankName, ignoreCase = true) }
                if (matchedAccount != null) {
                    accountId = matchedAccount.id
                }
            }

            // 2. Category Auto-assignment
            var categoryId: String? = null
            if (merchant != "Unknown" && merchant.isNotBlank()) {
                val pastTx = db.transactionDao().searchTransactions(merchant).first()
                val matchedCategoryTx = pastTx.firstOrNull { it.transaction.categoryId != null }
                if (matchedCategoryTx != null) {
                    categoryId = matchedCategoryTx.transaction.categoryId
                }
            }

            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val entity = TransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = accountId,
                categoryId = categoryId,
                amount = amountMinor,
                type = if (isExpense) TransactionType.EXPENSE.name else TransactionType.INCOME.name,
                note = "[PENDING] [SMS] $merchant",
                transactionDate = todayStr,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.transactionDao().insertTransaction(entity)
            
            // 4. Notifications
            sendNotification(context, amountMinor, merchant, bankName, isExpense)
        }
    }

    private fun sendNotification(context: Context, amountMinor: Long, merchant: String, bankName: String, isExpense: Boolean) {
        val channelId = "sms_transactions"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Smart Receipts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = String.format("%.2f", amountMinor / 100.0)
        val actionWord = if (isExpense) "added for" else "received from"
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Bluff Auto-Tracker")
            .setContentText("₹$amountStr $actionWord $merchant ($bankName)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(merchant.hashCode(), notification)
    }
}
