package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.CashFlowDao
import com.example.learncompose303.data.entities.CashFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCashFlowWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cashFlowDao: CashFlowDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCashFlowWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        cashFlowDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as CashFlow).toSyncDto()
        val response = syncApiService.syncCashFlows(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for CashFlow ${entity.cashFlowId}")
            else -> throw ServerSyncException("5xx $code for CashFlow ${entity.cashFlowId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        cashFlowDao.markSynced(listOf((entity as CashFlow).cashFlowId))
}
