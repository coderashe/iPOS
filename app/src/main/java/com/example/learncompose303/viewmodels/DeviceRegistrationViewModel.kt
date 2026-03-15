package com.example.learncompose303.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.app.core.DeviceManager
import com.example.learncompose303.data.data_source.DeviceInfoDao
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.entities.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    private val userDao: UserDao
) : ViewModel() {

    /** Save device info to Room (isSynced = false). SyncDeviceWorker handles the backend push. */
    fun registerDevice(deviceName: String, userEmail: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            deviceManager.setDeviceName(deviceName)
            val storeId = userDao.getUserByEmail(userEmail)?.storeID ?: ""
            deviceInfoDao.upsert(
                DeviceInfo(
                    deviceId = deviceManager.getDeviceId(),
                    deviceName = deviceName,
                    storeId = storeId,
                    isSynced = false
                )
            )
            onComplete()
        }
    }
}
