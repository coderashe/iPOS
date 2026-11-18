package com.ipos.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.WorkerParameters
import com.ipos.data.data_source.UserDao
import com.ipos.data.entities.User
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncUserWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userDao: UserDao,
    private val userApiService: UserApiService,
    connectivityHelper: ConnectivityHelper,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore,
    private val deviceSession: com.ipos.app.core.DeviceSession,
    private val deviceManager: com.ipos.app.core.DeviceManager,
    private val deviceInfoDao: com.ipos.data.data_source.DeviceInfoDao
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncUserWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> = userDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val user = entity as User
        var firestoreSynced = false
        val storeId = when {
            user.storeID.isNotBlank() -> user.storeID
            deviceSession.storeId.isNotBlank() -> deviceSession.storeId
            deviceManager.getStoreId().isNotBlank() -> deviceManager.getStoreId()
            else -> deviceInfoDao.getActiveStoreId() ?: ""
        }

        if (storeId.isBlank()) {
            Log.w("SyncUserWorker", "Cannot sync user: storeId is blank")
            return false
        }

        try {
            val map = hashMapOf(
                "userId" to user.userId,
                "fullName" to user.userFullName,
                "email" to user.userEmail,
                "role" to user.userRole,
                "contact" to user.userContact,
                "address" to user.userAddress,
                "storeId" to storeId
            )
            firestore.collection("stores").document(storeId)
                .collection("users").document(user.userId.toString())
                .set(map)
                .await()
            firestoreSynced = true
            Log.d("SyncUserWorker", "✔ Synced user ${user.userFullName} to Firestore")
        } catch (e: Exception) {
            Log.w("SyncUserWorker", "Firestore user sync error: ${e.message}")
        }

        var apiSynced = false
        try {
            val dto = UserDto(
                name = user.userFullName,
                contact = user.userContact,
                role = user.userRole,
                password = user.userPassword,
                email = user.userEmail,
                store = user.storeID,
                address = user.userAddress
            )
            val response = userApiService.saveUser(dto)
            if (response.isSuccessful) {
                apiSynced = true
            }
        } catch (e: Exception) {
            Log.w("SyncUserWorker", "REST API user sync skipped: ${e.message}")
        }

        return firestoreSynced || apiSynced
    }

    override suspend fun markSynced(entity: Any) =
        userDao.markSynced(listOf((entity as User).userId))
}
