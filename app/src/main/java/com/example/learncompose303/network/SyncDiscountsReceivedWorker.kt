package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.DiscountsReceivedDao
import com.example.learncompose303.data.entities.DiscountsReceived
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDiscountsReceivedWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val discountsReceivedDao: DiscountsReceivedDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncDiscountsReceivedWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        discountsReceivedDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as DiscountsReceived).toSyncDto()
        val response = syncApiService.syncDiscountsReceived(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for DiscountsReceived ${entity.discountId}")
            else -> throw ServerSyncException("5xx $code for DiscountsReceived ${entity.discountId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        discountsReceivedDao.markSynced(listOf((entity as DiscountsReceived).discountId))
}
