package com.ipos.network

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.app.core.DeviceSession
import com.ipos.app.core.DeviceSessionInitializer
import com.ipos.data.data_source.DayReportsDao
import com.ipos.data.entities.DayReports
import com.ipos.data.repositories.BackfillRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDayReportsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dayReportsDao: DayReportsDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper,
    deviceSession: DeviceSession,
    deviceSessionInitializer: DeviceSessionInitializer,
    backfillRepository: BackfillRepository
) : BaseSyncWorker(context, params, connectivityHelper, deviceSession, deviceSessionInitializer, backfillRepository) {

    interface Factory : WorkerAssistedFactory<SyncDayReportsWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        dayReportsDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val dto = (entity as DayReports).toSyncDto()
        val response = syncApiService.syncDayReports(listOf(dto))
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for DayReport ${entity.reportId}")
            else -> throw ServerSyncException("5xx $code for DayReport ${entity.reportId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        dayReportsDao.markSynced(listOf((entity as DayReports).reportId))
}
