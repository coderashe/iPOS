package com.example.learncompose303

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.learncompose303.workmanager.MonthlyLogoutWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//@HiltAndroidApp
//class MyApplication : Application()

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()

        // Schedule the worker
        scheduleMonthlyLogout()
    }

    private fun scheduleMonthlyLogout() {
        val workRequest = PeriodicWorkRequestBuilder<MonthlyLogoutWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Optional: Ensures logout happens when online
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "MonthlyLogoutWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d("MyApplication", "Monthly logout worker scheduled.")
    }
}
