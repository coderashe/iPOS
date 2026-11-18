package com.ipos.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.DeviceInfoDao
import com.ipos.data.entities.DeviceInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val deviceInfoDao: DeviceInfoDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val deviceSession: com.ipos.app.core.DeviceSession,
    private val deviceManager: com.ipos.app.core.DeviceManager
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncDeviceWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        deviceInfoDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val device = entity as DeviceInfo
        var firestoreSynced = false
        val storeId = when {
            device.storeId.isNotBlank() -> device.storeId
            deviceSession.storeId.isNotBlank() -> deviceSession.storeId
            deviceManager.getStoreId().isNotBlank() -> deviceManager.getStoreId()
            else -> deviceInfoDao.getActiveStoreId() ?: ""
        }

        if (storeId.isBlank()) {
            Log.w("SyncDeviceWorker", "Cannot sync device: storeId is blank")
            return false
        }

        try {
            val map = hashMapOf(
                "deviceId" to device.deviceId,
                "deviceName" to device.deviceName,
                "storeId" to storeId,
                "lastSeen" to device.lastSeen
            )
            firestore.collection("stores").document(storeId)
                .collection("devices").document(device.deviceId)
                .set(map)
                .await()
            firestoreSynced = true
            Log.d("SyncDeviceWorker", "✔ Synced device ${device.deviceId} to Firestore")
        } catch (e: Exception) {
            Log.w("SyncDeviceWorker", "Firestore device sync error: ${e.message}")
        }

        var apiSynced = false
        try {
            val response = syncApiService.syncDevices(listOf(device.toSyncDto()))
            if (response.isSuccessful) {
                apiSynced = true
            }
        } catch (e: Exception) {
            Log.w("SyncDeviceWorker", "REST API device sync skipped: ${e.message}")
        }

        return firestoreSynced || apiSynced
    }

    override suspend fun markSynced(entity: Any) =
        deviceInfoDao.markSynced((entity as DeviceInfo).deviceId)
}
