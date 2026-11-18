package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.app.core.DeviceManager
import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.CustomerDao
import com.ipos.data.data_source.DeviceInfoDao
import com.ipos.data.data_source.InvoiceDao
import com.ipos.data.data_source.ProductDao
import com.ipos.data.entities.DeviceInfo
import com.ipos.network.SyncManager
import com.ipos.data.repositories.BackfillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    val deviceSession: DeviceSession,
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val backfillRepository: BackfillRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    /** Save device info to Room (isSynced = false). SyncDeviceWorker handles the backend push. */
    fun registerDevice(deviceName: String, storeId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val cleanStoreId = storeId.trim()
            val cleanDeviceName = deviceName.trim()

            deviceManager.setDeviceName(cleanDeviceName)
            deviceManager.setStoreId(cleanStoreId)
            val deviceId = deviceManager.getDeviceId()

            deviceInfoDao.upsert(
                DeviceInfo(
                    deviceId = deviceId,
                    deviceName = cleanDeviceName,
                    storeId = cleanStoreId,
                    isSynced = false
                )
            )

            // Populate session immediately so backfill and sync use the correct storeId
            deviceSession.deviceId = deviceId
            deviceSession.storeId = cleanStoreId
            deviceSession.deviceName = cleanDeviceName

            // Re-stamp all tables and reset sync status on products, customers, and invoices
            try {
                backfillRepository.backfillDeviceAndStore(deviceId, cleanStoreId)
                deviceInfoDao.resetSyncStatus()
            } catch (e: Exception) {
                // Non-fatal
            }

            // Trigger sync immediately to push data under the registered store
            try {
                syncManager.syncAllNow()
            } catch (e: Exception) {
                // Non-fatal
            }

            onComplete()
        }
    }
}
