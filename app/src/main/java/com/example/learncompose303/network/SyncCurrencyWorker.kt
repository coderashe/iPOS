package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.entities.Currency
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCurrencyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val currencyDao: CurrencyDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCurrencyWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        currencyDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as Currency).toSyncDto()
        val response = syncApiService.syncCurrencies(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for Currency ${entity.code}")
            else -> throw ServerSyncException("5xx $code for Currency ${entity.code}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        currencyDao.markSynced(listOf((entity as Currency).code))
}
