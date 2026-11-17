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

@HiltWorker
class SyncUserWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userDao: UserDao,
    private val userApiService: UserApiService,
    connectivityHelper: ConnectivityHelper
) : BaseSyncWorker(context, params, connectivityHelper) {

    interface Factory : WorkerAssistedFactory<SyncUserWorker>

    override suspend fun fetchUnsynced(limit: Int): List<Any> = userDao.getUnsynced(limit)

    override suspend fun syncToApi(entity: Any): Boolean {
        val user = entity as User
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
        val code = response.code()
        return when {
            response.isSuccessful -> true
            code in 400..499 -> throw ClientSyncException("4xx $code for User ${user.userId}")
            else -> throw ServerSyncException("5xx $code for User ${user.userId}")
        }
    }

    override suspend fun markSynced(entity: Any) =
        userDao.markSynced(listOf((entity as User).userId))
}
