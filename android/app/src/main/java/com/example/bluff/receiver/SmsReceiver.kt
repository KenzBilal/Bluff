package com.example.bluff.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
        if (intent.action == "com.example.bluff.TEST_SMS") {
            val testSender = intent.getStringExtra("TEST_SENDER") ?: ""
            val testBody = intent.getStringExtra("TEST_BODY") ?: ""
            parseAndInsertTransaction(context, testSender, testBody)
            return
        }

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

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
    }

    private fun parseAndInsertTransaction(context: Context, sender: String, messageBody: String) {
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
            // "Dear UPI user A/C X5347 debited by 3000.00 on date 08Aug26 trf to ALEX JIJU Refno..."
            val sbiDebitRegex = Regex("debited by (\\d+(?:\\.\\d+)?).*?trf to (.*?)\\s+Refno", RegexOption.IGNORE_CASE)
            val match = sbiDebitRegex.find(messageBody)
            if (match != null) {
                val amountStr = match.groupValues[1]
                merchant = match.groupValues[2].trim()
                amountMinor = amountStr.toDoubleOrNull()?.times(100)?.toLong()
                isExpense = true
            }
        } else if (sender.contains("KOTAK", ignoreCase = true)) {
            // Expense: "Sent Rs.20316.00 from XX7045 to RAJESH  BHATIA on 10-Sep-26"
            val kotakSentRegex = Regex("Sent Rs\\.?(\\d+(?:\\.\\d+)?).*? to (.*?)\\s+on", RegexOption.IGNORE_CASE)
            // Income: "Received Rs.1.00 from ALEX  JIJU in your Kotak811 a/c"
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
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppContainer.instance.db
                val userId = UserPreferencesManager(context.applicationContext).getUserIdBlocking()
                
                // Try to find the first account
                val accounts = db.accountDao().getAllAccounts().first()
                val accountId = accounts.firstOrNull()?.id ?: return@launch

                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                val entity = TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    accountId = accountId,
                    categoryId = null, // Uncategorized for SMS
                    amount = amountMinor,
                    type = if (isExpense) TransactionType.EXPENSE.name else TransactionType.INCOME.name,
                    note = "[SMS] $merchant",
                    transactionDate = todayStr,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                db.transactionDao().insertTransaction(entity)
            }
        }
    }
}
