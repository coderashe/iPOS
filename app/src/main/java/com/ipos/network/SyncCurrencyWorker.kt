package com.ipos.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.ipos.app.core.DeviceManager
import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.CurrencyDao
import com.ipos.data.data_source.DeviceInfoDao
import com.ipos.data.entities.Currency
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncCurrencyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val currencyDao: CurrencyDao,
    private val syncApiService: SyncApiService,
    private val firestore: FirebaseFirestore,
    private val deviceSession: DeviceSession,
    private val deviceManager: DeviceManager,
    private val deviceInfoDao: DeviceInfoDao,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCurrencyWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        currencyDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val currency = entity as Currency

        val storeId = when {
            deviceSession.storeId.isNotBlank() -> deviceSession.storeId
            deviceManager.getStoreId().isNotBlank() -> deviceManager.getStoreId()
            else -> deviceInfoDao.getActiveStoreId() ?: ""
        }

        var firestoreOk = false
        if (storeId.isNotBlank()) {
            try {
                val map = hashMapOf(
                    "code" to currency.code,
                    "name" to currency.name,
                    "rateToBase" to currency.rateToBase,
                    "storeId" to storeId,
                    "lastUpdated" to System.currentTimeMillis()
                )
                firestore.collection("stores").document(storeId)
                    .collection("currencies").document(currency.code)
                    .set(map)
                    .await()
                firestoreOk = true
                Log.d("SyncCurrencyWorker", "✔ Synced currency ${currency.code} (rate: ${currency.rateToBase}) to Firestore under stores/$storeId/currencies")
            } catch (e: Exception) {
                Log.w("SyncCurrencyWorker", "Firestore currency sync error: ${e.message}")
            }
        }

        var apiOk = false
        try {
            val dto = currency.toSyncDto()
            val response = syncApiService.syncCurrencies(listOf(dto))
            apiOk = response.isSuccessful
        } catch (e: Exception) {
            Log.w("SyncCurrencyWorker", "REST API currency sync skipped: ${e.message}")
        }

        return firestoreOk || apiOk
    }

    override suspend fun markSynced(entity: Any) =
        currencyDao.markSynced(listOf((entity as Currency).code))
}
