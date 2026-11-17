package com.ipos.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.CustomerDao
import com.ipos.data.entities.Customer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncCustomerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val customerDao: CustomerDao,
    private val syncApiService: SyncApiService,
    connectivityHelper: ConnectivityHelper,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncCustomerWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> =
        customerDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val customer = entity as Customer
        var firestoreSynced = false
        val storeId = "ISTART"

        try {
            val map = hashMapOf(
                "customerId" to customer.customerId,
                "name" to customer.name,
                "email" to customer.email,
                "phone" to customer.phone,
                "address" to customer.address,
                "customerType" to customer.customerType,
                "loyaltyPoints" to customer.loyaltyPoints,
                "storeId" to storeId
            )
            firestore.collection("stores").document(storeId)
                .collection("customers").document(customer.customerId.toString())
                .set(map)
                .await()
            firestoreSynced = true
            Log.d("SyncCustomerWorker", "✔ Synced customer ${customer.name} to Firestore")
        } catch (e: Exception) {
            Log.w("SyncCustomerWorker", "Firestore customer sync error: ${e.message}")
        }

        var apiSynced = false
        try {
            val dto = customer.toSyncDto()
            val response = syncApiService.syncCustomers(listOf(dto))
            if (response.isSuccessful) {
                apiSynced = true
            }
        } catch (e: Exception) {
            Log.w("SyncCustomerWorker", "REST API customer sync skipped: ${e.message}")
        }

        return firestoreSynced || apiSynced
    }

    override suspend fun markSynced(entity: Any) =
        customerDao.markSynced(listOf((entity as Customer).customerId))
}
