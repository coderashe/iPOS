package com.example.learncompose303.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.app.core.DeviceManager
import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.data.data_source.DeviceInfoDao
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.entities.DeviceInfo
import com.example.learncompose303.data.repositories.BackfillRepository
import com.example.learncompose303.network.SyncApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StepState(
    val label: String,
    val status: StepStatus = StepStatus.PENDING
)

enum class StepStatus { PENDING, RUNNING, DONE, ERROR }

data class SetupUiState(
    val steps: List<StepState> = listOf(
        StepState("Loading device session"),
        StepState("Updating existing records"),
        StepState("Syncing device to server")
    ),
    val isComplete: Boolean = false,
    val canRetry: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val deviceManager: DeviceManager,
    private val deviceSession: DeviceSession,
    private val deviceInfoDao: DeviceInfoDao,
    private val userDao: UserDao,
    private val backfillRepository: BackfillRepository,
    private val syncApiService: SyncApiService,
    private val syncManager: com.example.learncompose303.network.SyncManager
) : ViewModel() {

    companion object {
        private const val TAG = "SetupViewModel"
        private const val PREFS_NAME = "setup_prefs"
        private const val KEY_SETUP_DONE = "setup_complete"
    }

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun runSetup(context: android.content.Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

            // Reset to initial state
            _uiState.value = SetupUiState()

            // ── Step 0: Load device session ──────────────────────────────────
            setStep(0, StepStatus.RUNNING)
            try {
                val deviceId = deviceManager.getDeviceId()
                val info = deviceInfoDao.getByDeviceId(deviceId)
                if (info != null) {
                    deviceSession.deviceId = info.deviceId
                    deviceSession.storeId = info.storeId
                    deviceSession.deviceName = info.deviceName
                    Log.d(TAG, "Session loaded — deviceId=${info.deviceId} storeId=${info.storeId}")
                } else {
                    // Device not registered yet — try to get storeId from logged-in user
                    val users = userDao.getAllUsersOnce()
                    val storeId = users.firstOrNull()?.storeID ?: ""
                    val deviceName = deviceManager.getDeviceName()
                    deviceSession.deviceId = deviceId
                    deviceSession.storeId = storeId
                    deviceSession.deviceName = deviceName
                    // Persist to Room so future launches load it
                    deviceInfoDao.upsert(
                        DeviceInfo(
                            deviceId = deviceId,
                            deviceName = deviceName,
                            storeId = storeId,
                            isSynced = false
                        )
                    )
                    Log.d(TAG, "New device registered — deviceId=$deviceId storeId=$storeId")
                }
                setStep(0, StepStatus.DONE)
            } catch (e: Exception) {
                Log.e(TAG, "Step 0 failed: ${e.message}", e)
                setStep(0, StepStatus.ERROR)
                setError("Failed to load device session: ${e.message}")
                return@launch
            }

            // ── Step 1: Backfill existing records ────────────────────────────
            setStep(1, StepStatus.RUNNING)
            try {
                if (deviceSession.deviceId.isNotBlank() && deviceSession.storeId.isNotBlank()) {
                    backfillRepository.backfillDeviceAndStore(
                        deviceId = deviceSession.deviceId,
                        storeId = deviceSession.storeId
                    )
                    Log.d(TAG, "Backfill complete")
                } else {
                    Log.w(TAG, "Skipping backfill — storeId still blank (user may not be logged in yet)")
                }
                setStep(1, StepStatus.DONE)
            } catch (e: Exception) {
                Log.e(TAG, "Step 1 failed: ${e.message}", e)
                setStep(1, StepStatus.ERROR)
                setError("Failed to update records: ${e.message}")
                return@launch
            }

            // ── Step 2: Sync device to server ────────────────────────────────
            setStep(2, StepStatus.RUNNING)
            try {
                val deviceInfo = deviceInfoDao.getByDeviceId(deviceSession.deviceId)
                if (deviceInfo != null) {
                    val dto = com.example.learncompose303.network.DeviceSyncDto(
                        deviceId = deviceInfo.deviceId,
                        deviceName = deviceInfo.deviceName,
                        storeId = deviceInfo.storeId,
                        lastSeen = System.currentTimeMillis()
                    )
                    val response = syncApiService.syncDevices(listOf(dto))
                    if (response.isSuccessful) {
                        deviceInfoDao.markSynced(deviceInfo.deviceId)
                        Log.d(TAG, "Device synced to server successfully")
                    } else {
                        Log.w(TAG, "Server returned ${response.code()} — will retry via WorkManager")
                        // Non-fatal: WorkManager will retry. Don't block the user.
                    }
                }
                setStep(2, StepStatus.DONE)
            } catch (e: Exception) {
                // Network errors are non-fatal for this step — WorkManager handles retries
                Log.w(TAG, "Step 2 network error (non-fatal): ${e.message}")
                setStep(2, StepStatus.DONE) // Mark done anyway, WorkManager retries
            }

            // ── All done ─────────────────────────────────────────────────────
            // Block if storeId is still blank — user must complete device registration
            if (deviceSession.storeId.isBlank()) {
                setError("This device has no Store ID. Please complete device registration before continuing.")
                return@launch
            }

            prefs.edit().putBoolean(KEY_SETUP_DONE, true).apply()
            _uiState.value = _uiState.value.copy(isComplete = true)
            Log.d(TAG, "Setup complete")

            // Trigger full sync now that backfill is done and storeId is set
            syncManager.syncAllNow()
            Log.d(TAG, "Full sync scheduled after setup")
        }
    }

    fun retry(context: android.content.Context) {
        runSetup(context)
    }

    fun runBackfill() {
        viewModelScope.launch {
            if (deviceSession.deviceId.isNotBlank() && deviceSession.storeId.isNotBlank()) {
                Log.d(TAG, "Manual backfill triggered")
                backfillRepository.backfillDeviceAndStore(
                    deviceId = deviceSession.deviceId,
                    storeId = deviceSession.storeId
                )
                Log.d(TAG, "Manual backfill complete")
            } else {
                Log.w(TAG, "Manual backfill skipped — deviceId or storeId blank")
            }
        }
    }

    /** Returns true only if setup completed AND this device has a valid storeId.
     *  If storeId is missing, clears the flag so setup re-runs on next check. */
    fun isSetupAlreadyDone(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val flagSet = prefs.getBoolean(KEY_SETUP_DONE, false)
        if (!flagSet) return false
        // Double-check the session is actually populated (guards against app data clear / reinstall)
        if (deviceSession.storeId.isBlank()) {
            prefs.edit().remove(KEY_SETUP_DONE).apply()
            return false
        }
        // Always re-run backfill in background even if setup was already done
        viewModelScope.launch {
            if (deviceSession.deviceId.isNotBlank() && deviceSession.storeId.isNotBlank()) {
                backfillRepository.backfillDeviceAndStore(
                    deviceId = deviceSession.deviceId,
                    storeId = deviceSession.storeId
                )
            }
        }
        return true
    }

    private fun setStep(index: Int, status: StepStatus) {
        val steps = _uiState.value.steps.toMutableList()
        steps[index] = steps[index].copy(status = status)
        _uiState.value = _uiState.value.copy(steps = steps, canRetry = false, errorMessage = null)
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(canRetry = true, errorMessage = message)
    }
}
