package com.example.learncompose303.app.core

import com.example.learncompose303.data.data_source.DeviceInfoDao
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
    private val deviceSession: DeviceSession
) {
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

    private suspend fun loadSession() {
        val info = deviceInfoDao.getByDeviceId(deviceManager.getDeviceId())
        if (info != null) {
            deviceSession.deviceId = info.deviceId
            deviceSession.storeId = info.storeId
            deviceSession.deviceName = info.deviceName
        }
    }
}
