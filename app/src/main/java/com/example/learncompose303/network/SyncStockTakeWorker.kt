package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.StockTakeDao
import com.example.learncompose303.data.entities.StockTake
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncStockTakeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val stockTakeDao: StockTakeDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncStockTakeWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        stockTakeDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as StockTake).toSyncDto()
        val response = syncApiService.syncStockTakes(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for StockTake ${entity.id}")
            else -> throw ServerSyncException("5xx $code for StockTake ${entity.id}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        stockTakeDao.markSynced(listOf((entity as StockTake).id))
}
