package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.ProductDayReportDao
import com.example.learncompose303.data.entities.ProductDayReport
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncProductDayReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val productDayReportDao: ProductDayReportDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncProductDayReportWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        productDayReportDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as ProductDayReport).toSyncDto()
        val response = syncApiService.syncProductDayReports(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for ProductDayReport ${entity.reportId}")
            else -> throw ServerSyncException("5xx $code for ProductDayReport ${entity.reportId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        productDayReportDao.markSynced(listOf((entity as ProductDayReport).reportId))
}
