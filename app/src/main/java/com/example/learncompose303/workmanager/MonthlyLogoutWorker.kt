package com.example.learncompose303.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log
import java.util.Calendar
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MonthlyLogoutWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // Check if today is the 1st of the month
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            Log.d("MonthlyLogoutWorker", "Logging out user on the 1st of the month.")
            logoutUser()  // Call your logout function here
        } else {
            Log.d("MonthlyLogoutWorker", "Not the 1st of the month, skipping logout.")
        }

        return Result.success()
    }

    private fun logoutUser() {
        // Perform Firebase logout
        FirebaseAuth.getInstance().signOut()
        Log.d("MonthlyLogoutWorker", "User successfully logged out.")
    }

    private fun scheduleMonthlyLogout() {
        // Create a PeriodicWorkRequest to run daily
        val logoutWorkRequest =
            PeriodicWorkRequestBuilder<MonthlyLogoutWorker>(1, TimeUnit.DAYS) // Repeat every day
                .build()

        // Enqueue the work with WorkManager
        WorkManager.getInstance(applicationContext).enqueue(logoutWorkRequest)
    }
}
