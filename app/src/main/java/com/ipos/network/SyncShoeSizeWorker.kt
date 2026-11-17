package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.ShoeSizeDao
import com.ipos.data.entities.ShoeSize
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncShoeSizeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shoeSizeDao: ShoeSizeDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncShoeSizeWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        shoeSizeDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as ShoeSize).toSyncDto()
        val response = syncApiService.syncShoeSizes(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for ShoeSize ${entity.shoeSizeId}")
            else -> throw ServerSyncException("5xx $code for ShoeSize ${entity.shoeSizeId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        shoeSizeDao.markSynced(listOf((entity as ShoeSize).shoeSizeId))
}
