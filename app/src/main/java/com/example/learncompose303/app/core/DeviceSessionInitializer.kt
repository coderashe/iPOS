package com.example.learncompose303.app.core

import com.example.learncompose303.data.data_source.DeviceInfoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSessionInitializer @Inject constructor(
    private val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    private val deviceSession: DeviceSession
) {
    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            val info = deviceInfoDao.getByDeviceId(deviceManager.getDeviceId())
            if (info != null) {
                deviceSession.deviceId = info.deviceId
                deviceSession.storeId = info.storeId
                deviceSession.deviceName = info.deviceName
            }
        }
    }
}
