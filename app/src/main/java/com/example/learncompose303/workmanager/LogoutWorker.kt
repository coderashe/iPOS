package com.example.learncompose303.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import java.util.Calendar
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.learncompose303.MyApplication
import com.example.learncompose303.screens.auth.AuthState
import com.example.learncompose303.screens.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LogoutWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val authViewModel: AuthViewModel
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Set authState to UnAuthenticated
        val checkStatus = authViewModel.setAuthState(AuthState.UnAuthenticated)
        FirebaseAuth.getInstance().signOut()
        Log.d("AuthViewModel", "Auth status is $checkStatus")
        Log.d("AuthViewModel", "User has been logged out.")

        return Result.success()
    }
}
