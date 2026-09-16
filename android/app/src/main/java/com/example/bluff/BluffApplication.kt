package com.example.bluff

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.sync.RecurringWorker
import com.example.bluff.di.AppContainer
import java.util.concurrent.TimeUnit

class BluffApplication : Application(), Configuration.Provider {

    private val workerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                RecurringWorker::class.java.name ->
                    RecurringWorker(appContext, workerParameters, AppContainer.instance.db)
                else -> null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)

        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
