package com.example.learncompose303.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import java.util.Calendar
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.learncompose303.data.entities.User
import com.example.learncompose303.network.SyncUserWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MonthlyLogoutWorker @Inject constructor(
    @ApplicationContext context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (currentDay == 1) {
            val sharedPreferences =
                applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val lastLogoutDate = sharedPreferences.getInt("lastLogoutDay", -1)

            if (lastLogoutDate != 1) {
                logoutUser()
                sharedPreferences.edit().putInt("lastLogoutDay", 1).apply()
            } else {
                Log.d("MonthlyLogoutWorker", "User already logged out today, skipping.")
            }
        } else {
            Log.d("MonthlyLogoutWorker", "Not the 1st of the month, skipping logout.")
        }

        return Result.success()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Log.d("MonthlyLogoutWorker", "User successfully logged out.")
    }
}
