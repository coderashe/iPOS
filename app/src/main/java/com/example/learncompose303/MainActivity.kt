package com.example.learncompose303

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.ui.theme.LearnCompose303Theme
import com.example.learncompose303.viewmodels.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.app.SwipeRefreshLayout
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.learncompose303.network.SyncInvoiceWorker
import com.example.learncompose303.workmanager.MonthlyLogoutWorker
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private var isWorkScheduled = false

    override fun onStart() {
        super.onStart()

        if (!isWorkScheduled) {
            setupPeriodicSync()
            scheduleMonthlyLogout()
            isWorkScheduled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the permission is already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        }
        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
        setContent {
            LearnCompose303Theme {
                // Create navController at the root level
                val navController = rememberNavController()
                val viewModel: InvoiceViewModel = viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SwipeRefreshLayout(viewModel, navController)
                }
            }
        }
    }
    /**
     * Setup periodic sync to ensure nothing is missed
     */
    fun setupPeriodicSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncInvoiceWorker>(
            30, TimeUnit.MINUTES
        )
            .setInputData(
                workDataOf(
                    SyncInvoiceWorker.KEY_BATCH_SYNC to true,
                    SyncInvoiceWorker.KEY_RETRY to true
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "invoice_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
        Log.d("SyncInvoiceWorker", "SyncInvoiceWorker scheduled.")
    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                Log.d("MainActivity", "Notification permission granted.")
            } else {
                // Permission denied
                Log.d("MainActivity", "Notification permission denied.")
            }
        }
    }

    private fun scheduleMonthlyLogout() {
        val workRequest = PeriodicWorkRequestBuilder<MonthlyLogoutWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MonthlyLogoutWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d("MyApplication", "Monthly logout worker scheduled.")
    }

}



