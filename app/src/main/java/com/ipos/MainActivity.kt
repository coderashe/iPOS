package com.ipos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipos.ui.theme.LearnCompose303Theme
import com.ipos.viewmodels.InvoiceViewModel
import com.ipos.viewmodels.TrialViewModel
import com.ipos.app.core.TrialManager
import com.ipos.screens.SetupScreen
import com.ipos.screens.DeviceRegistrationScreen
import com.ipos.screens.TrialBanner
import com.ipos.screens.TrialExpiredScreen
import com.ipos.viewmodels.SetupViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import androidx.navigation.compose.rememberNavController
import com.ipos.app.SwipeRefreshLayout
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ipos.network.SyncInvoiceWorker
import com.ipos.network.SyncProductWorker
import com.ipos.workmanager.MonthlyLogoutWorker
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private var isWorkScheduled = false

    override fun onStart() {
        super.onStart()

        if (!isWorkScheduled) {
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
                val trialViewModel: TrialViewModel = hiltViewModel()
                val trialStatus by trialViewModel.trialStatus.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val status = trialStatus) {
                        is TrialManager.TrialStatus.Loading -> {
                            // Show a centered spinner while Firestore responds
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is TrialManager.TrialStatus.Expired -> {
                            TrialExpiredScreen()
                        }
                        is TrialManager.TrialStatus.Active -> {
                            val navController = rememberNavController()
                            val viewModel: InvoiceViewModel = viewModel()
                            val setupViewModel: SetupViewModel = hiltViewModel()
                            val context = androidx.compose.ui.platform.LocalContext.current

                            // Show setup gate if not yet completed
                            var setupDone by remember {
                                mutableStateOf(setupViewModel.isSetupAlreadyDone(context))
                            }
                            var showDeviceRegistration by remember { mutableStateOf(false) }

                            Column(modifier = Modifier.fillMaxSize()) {
                                TrialBanner(daysLeft = status.daysLeft)
                                when {
                                    showDeviceRegistration -> {
                                        DeviceRegistrationScreen(
                                            onRegistrationComplete = {
                                                showDeviceRegistration = false
                                                // Re-run setup now that device is registered
                                                setupViewModel.retry(context)
                                            }
                                        )
                                    }
                                    !setupDone -> {
                                        SetupScreen(
                                            onSetupComplete = { setupDone = true },
                                            onRegisterDevice = { showDeviceRegistration = true },
                                            viewModel = setupViewModel
                                        )
                                    }
                                    else -> {
                                        SwipeRefreshLayout(viewModel, navController)
                                    }
                                }
                            }
                        }
                    }
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

    // In your MainActivity or wherever you schedule the invoice sync
    fun setupPeriodicProductSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncProductWorker>(
            30, TimeUnit.MINUTES
        )
            .setInputData(
                workDataOf(
                    SyncProductWorker.KEY_BATCH_SYNC to true,
                    SyncProductWorker.KEY_RETRY to true
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "product_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
        Log.d("SyncProductWorker", "SyncProductWorker scheduled.")
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
