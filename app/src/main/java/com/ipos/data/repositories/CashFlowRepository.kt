package com.ipos.data.repositories

import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.CashFlowDao
import com.ipos.data.entities.CashFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CashFlowRepository {
    suspend fun insertCashFlow(cashFlow: CashFlow)
    fun getAllCashFlows(): Flow<List<CashFlow>>
    suspend fun deleteCashFlow(cashFlow: CashFlow)
}

class CashFlowRepositoryImpl @Inject constructor(
    private val cashFlowDao: CashFlowDao,
    private val deviceSession: DeviceSession
) : CashFlowRepository {

    override suspend fun insertCashFlow(cashFlow: CashFlow) {
        withContext(Dispatchers.IO) {
            cashFlowDao.insertCashFlow(
                cashFlow.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }

    override fun getAllCashFlows(): Flow<List<CashFlow>> = cashFlowDao.getAllCashFlows()

    override suspend fun deleteCashFlow(cashFlow: CashFlow) {
        withContext(Dispatchers.IO) { cashFlowDao.deleteCashFlow(cashFlow) }
    }
}
