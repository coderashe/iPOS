package com.example.learncompose303.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.app.core.DeviceManager
import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.data.data_source.DeviceInfoDao
import com.example.learncompose303.data.entities.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    val deviceSession: DeviceSession
) : ViewModel() {

    /** Save device info to Room (isSynced = false). SyncDeviceWorker handles the backend push. */
    fun registerDevice(deviceName: String, storeId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            deviceManager.setDeviceName(deviceName)
            val deviceId = deviceManager.getDeviceId()
            deviceInfoDao.upsert(
                DeviceInfo(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    storeId = storeId,
                    isSynced = false
                )
            )
            // Populate session immediately so backfill and sync use the correct storeId
            deviceSession.deviceId = deviceId
            deviceSession.storeId = storeId
            deviceSession.deviceName = deviceName
            onComplete()
        }
    }
}
