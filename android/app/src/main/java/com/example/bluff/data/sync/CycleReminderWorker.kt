package com.example.bluff.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluff.R
import com.example.bluff.data.local.BluffDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CycleReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val db: BluffDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val twoDaysFromNow = today.plusDays(2)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val dueCycles = db.expenseCycleDao().getDueForReminder(twoDaysFromNow.format(formatter))

        if (dueCycles.isNotEmpty()) {
            createNotificationChannel()

            dueCycles.forEach { cycle ->
                val daysUntil = ChronoUnit.DAYS.between(today, LocalDate.parse(cycle.nextDueDate, formatter))
                val message = when {
                    daysUntil < 0 -> "${cycle.name} was due ${-daysUntil} days ago"
                    daysUntil == 0L -> "${cycle.name} is due today"
                    daysUntil == 1L -> "${cycle.name} is due tomorrow"
                    else -> "${cycle.name} is due in $daysUntil days"
                }

                sendNotification(cycle.id.hashCode(), cycle.name, message)
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming expenses"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "expense_reminders"
    }
}
