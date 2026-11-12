package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.TradeReceivablesDao
import com.ipos.data.entities.TradeReceivables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TradeReceivablesRepository {
    suspend fun insertTradeReceivable(tradeReceivable: TradeReceivables)
    fun getAllTradeReceivables(): Flow<List<TradeReceivables>>
    fun getOverdueReceivables(currentTime: Long): Flow<List<TradeReceivables>>
    suspend fun updateTradeReceivable(tradeReceivable: TradeReceivables)
    suspend fun deleteTradeReceivable(tradeReceivable: TradeReceivables)
}

class TradeReceivablesRepositoryImpl @Inject constructor(
    private val tradeReceivablesDao: TradeReceivablesDao,
    private val deviceSession: DeviceSession
) : TradeReceivablesRepository {

    override suspend fun insertTradeReceivable(tradeReceivable: TradeReceivables) {
        withContext(Dispatchers.IO) {
            tradeReceivablesDao.insertTradeReceivable(
                tradeReceivable.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }

    override fun getAllTradeReceivables(): Flow<List<TradeReceivables>> =
        tradeReceivablesDao.getAllTradeReceivables()

    override fun getOverdueReceivables(currentTime: Long): Flow<List<TradeReceivables>> =
        tradeReceivablesDao.getOverdueReceivables(currentTime)

    override suspend fun updateTradeReceivable(tradeReceivable: TradeReceivables) =
        withContext(Dispatchers.IO) { tradeReceivablesDao.updateTradeReceivable(tradeReceivable) }

    override suspend fun deleteTradeReceivable(tradeReceivable: TradeReceivables) =
        withContext(Dispatchers.IO) { tradeReceivablesDao.deleteTradeReceivable(tradeReceivable) }
}
