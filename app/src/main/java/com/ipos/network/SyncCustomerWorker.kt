package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.CustomerDao
import com.ipos.data.entities.Customer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCustomerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val customerDao: CustomerDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCustomerWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        customerDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as Customer).toSyncDto()
        val response = syncApiService.syncCustomers(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for Customer ${entity.customerId}")
            else -> throw ServerSyncException("5xx $code for Customer ${entity.customerId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        customerDao.markSynced(listOf((entity as Customer).customerId))
}
