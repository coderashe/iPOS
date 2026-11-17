package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.ShoeBrandDao
import com.ipos.data.entities.ShoeBrand
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncShoeBrandWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shoeBrandDao: ShoeBrandDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncShoeBrandWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        shoeBrandDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as ShoeBrand).toSyncDto()
        val response = syncApiService.syncShoeBrands(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for ShoeBrand ${entity.brandID}")
            else -> throw ServerSyncException("5xx $code for ShoeBrand ${entity.brandID}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        shoeBrandDao.markSynced(listOf((entity as ShoeBrand).brandID))
}
