package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.app.core.DeviceSession
import com.ipos.app.core.DeviceSessionInitializer
import com.ipos.data.data_source.PaymentDao
import com.ipos.data.entities.Payment
import com.ipos.data.repositories.BackfillRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPaymentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val paymentDao: PaymentDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper,
    deviceSession: DeviceSession,
    deviceSessionInitializer: DeviceSessionInitializer,
    backfillRepository: BackfillRepository
) : BaseSyncWorker(context, params, connectivityHelper, deviceSession, deviceSessionInitializer, backfillRepository) {

    interface Factory : WorkerAssistedFactory<SyncPaymentWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        paymentDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as Payment).toSyncDto()
        val response = syncApiService.syncPayments(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for Payment ${entity.paymentId}")
            else -> throw ServerSyncException("5xx $code for Payment ${entity.paymentId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        paymentDao.markSynced(listOf((entity as Payment).paymentId))
}
