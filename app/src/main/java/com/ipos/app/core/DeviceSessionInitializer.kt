package com.ipos.app.core

import android.util.Log
import com.ipos.data.data_source.DeviceInfoDao
import com.ipos.data.data_source.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSessionInitializer @Inject constructor(
    private val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    private val deviceSession: DeviceSession,
    private val userDao: UserDao
) {
    companion object {
        private const val TAG = "DeviceSessionInit"
    }

    /** Fire-and-forget init called at app startup. Returns the Job so callers can join if needed. */
    fun init(): Job = CoroutineScope(Dispatchers.IO).launch {
        loadSession()
    }

    /** Suspend version — awaitable by sync workers before they start. */
    suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        if (!deviceSession.isRegistered()) {
            loadSession()
        }
    }

    suspend fun loadSession() {
        var info = deviceInfoDao.getByDeviceId(deviceManager.getDeviceId())
        if (info == null) {
            info = deviceInfoDao.getActiveDevice()
        }

        val prefsStoreId = deviceManager.getStoreId()
        val userStoreId = try {
            userDao.getAllUsersOnce().firstOrNull { it.storeID.isNotBlank() }?.storeID ?: ""
        } catch (e: Exception) {
            ""
        }

        val resolvedStoreId = when {
            !info?.storeId.isNullOrBlank() -> info!!.storeId
            prefsStoreId.isNotBlank() -> prefsStoreId
            userStoreId.isNotBlank() -> userStoreId
            else -> ""
        }.trim()

        val resolvedDeviceId = info?.deviceId ?: deviceManager.getDeviceId()
        val resolvedDeviceName = info?.deviceName ?: deviceManager.getDeviceName()

        deviceSession.deviceId = resolvedDeviceId
        deviceSession.storeId = resolvedStoreId
        deviceSession.deviceName = resolvedDeviceName

        if (resolvedStoreId.isNotBlank()) {
            deviceManager.setStoreId(resolvedStoreId)
        }

        Log.d(TAG, "DeviceSession loaded: deviceId=$resolvedDeviceId, storeId=$resolvedStoreId, deviceName=$resolvedDeviceName")
    }
}
