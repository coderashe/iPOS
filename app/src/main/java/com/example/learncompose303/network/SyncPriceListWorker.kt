package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.entities.PriceList
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPriceListWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val priceListDao: PriceListDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncPriceListWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        priceListDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as PriceList).toSyncDto()
        val response = syncApiService.syncPriceLists(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for PriceList ${entity.priceListId}")
            else -> throw ServerSyncException("5xx $code for PriceList ${entity.priceListId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        priceListDao.markSynced(listOf((entity as PriceList).priceListId))
}
