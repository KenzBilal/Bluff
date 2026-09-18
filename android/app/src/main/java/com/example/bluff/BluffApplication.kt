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
import com.example.bluff.data.local.entity.CategoryCycleDefaultEntity
import com.example.bluff.data.sync.CycleReminderWorker
import com.example.bluff.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BluffApplication : Application(), Configuration.Provider {

    private val workerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {

                CycleReminderWorker::class.java.name ->
                    CycleReminderWorker(appContext, workerParameters, AppContainer.instance.db)
                else -> null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)

        CoroutineScope(Dispatchers.IO).launch {
            val defaults = listOf(
                CategoryCycleDefaultEntity("cat_haircut", "Haircut", 30),
                CategoryCycleDefaultEntity("cat_shaving", "Shaving", 15),
                CategoryCycleDefaultEntity("cat_car_service", "Car Service", 180),
                CategoryCycleDefaultEntity("cat_insurance", "Insurance", 365),
                CategoryCycleDefaultEntity("cat_electricity", "Electricity", 30),
                CategoryCycleDefaultEntity("cat_gas_cylinder", "Gas Cylinder", 60),
                CategoryCycleDefaultEntity("cat_internet", "Internet", 30),
                CategoryCycleDefaultEntity("cat_mobile_recharge", "Mobile Recharge", 28)
            )
            AppContainer.instance.db.categoryCycleDefaultDao().insertAll(defaults)
        }

        val cycleReminderWorkRequest = PeriodicWorkRequestBuilder<CycleReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cycle_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            cycleReminderWorkRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
