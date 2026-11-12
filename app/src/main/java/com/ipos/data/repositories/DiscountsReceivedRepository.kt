package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.DiscountsReceivedDao
import com.ipos.data.entities.DiscountsReceived
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DiscountsReceivedRepository {
    suspend fun insertDiscountReceived(discountReceived: DiscountsReceived)
    fun getAllDiscountsReceived(): Flow<List<DiscountsReceived>>
    suspend fun deleteDiscountReceived(discountReceived: DiscountsReceived)
}

class DiscountsReceivedRepositoryImpl @Inject constructor(
    private val discountsReceivedDao: DiscountsReceivedDao,
    private val deviceSession: DeviceSession
) : DiscountsReceivedRepository {

    override suspend fun insertDiscountReceived(discountReceived: DiscountsReceived) {
        withContext(Dispatchers.IO) {
            discountsReceivedDao.insertDiscountReceived(
                discountReceived.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }

    override fun getAllDiscountsReceived(): Flow<List<DiscountsReceived>> =
        discountsReceivedDao.getAllDiscountsReceived()

    override suspend fun deleteDiscountReceived(discountReceived: DiscountsReceived) =
        withContext(Dispatchers.IO) { discountsReceivedDao.deleteDiscountReceived(discountReceived) }
}
