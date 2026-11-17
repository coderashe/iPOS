package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.app.core.DeviceSession
import com.ipos.app.core.DeviceSessionInitializer
import com.ipos.data.data_source.ExpenseDao
import com.ipos.data.entities.Expense
import com.ipos.data.repositories.BackfillRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncExpenseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val expenseDao: ExpenseDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper,
    deviceSession: DeviceSession,
    deviceSessionInitializer: DeviceSessionInitializer,
    backfillRepository: BackfillRepository
) : BaseSyncWorker(context, params, connectivityHelper, deviceSession, deviceSessionInitializer, backfillRepository) {

    interface Factory : WorkerAssistedFactory<SyncExpenseWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        expenseDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as Expense).toSyncDto()
        val response = syncApiService.syncExpenses(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for Expense ${entity.expenseId}")
            else -> throw ServerSyncException("5xx $code for Expense ${entity.expenseId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        expenseDao.markSynced(listOf((entity as Expense).expenseId))
}
