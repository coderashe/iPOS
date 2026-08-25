package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.DeviceInfoDao
import com.example.learncompose303.data.entities.DeviceInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDeviceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val deviceInfoDao: DeviceInfoDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncDeviceWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        deviceInfoDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val device = entity as DeviceInfo
        val response = syncApiService.syncDevices(listOf(device.toSyncDto()))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for Device ${device.deviceId}")
            else -> throw ServerSyncException("5xx $code for Device ${device.deviceId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        deviceInfoDao.markSynced((entity as DeviceInfo).deviceId)
}
