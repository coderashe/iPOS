package com.example.learncompose303.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.entities.CartItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCartItemWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cartItemDao: CartItemDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCartItemWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        cartItemDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as CartItem).toSyncDto()
        val response = syncApiService.syncCartItems(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for CartItem ${entity.cartItemId}")
            else -> throw ServerSyncException("5xx $code for CartItem ${entity.cartItemId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        cartItemDao.markSynced(listOf((entity as CartItem).cartItemId))
}
