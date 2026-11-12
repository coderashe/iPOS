package com.ipos

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ipos.app.core.DeviceSessionInitializer
import com.ipos.network.SyncInvoiceWorker
import com.ipos.workmanager.MonthlyLogoutWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var deviceSessionInitializer: DeviceSessionInitializer

    override fun onCreate() {
        super.onCreate()
        deviceSessionInitializer.init()
        Log.d("SyncInvoiceWorker", "Application created")
    }

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("SyncInvoiceWorker", "Getting WorkManager configuration")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        }
}

