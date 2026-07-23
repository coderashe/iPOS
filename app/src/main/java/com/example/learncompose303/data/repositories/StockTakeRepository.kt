package com.example.learncompose303.data.repositories

import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.data.data_source.StockTakeDao
import com.example.learncompose303.data.data_source.StockTakeSessionDao
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.data.entities.StockTakeSession
import com.example.learncompose303.data.entities.StockTakeSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface StockTakeRepository {
    // Legacy / sync
    fun getAllStockTakes(): Flow<List<StockTake>>
    suspend fun insertStockTake(stockTake: StockTake)

    // Session management
    fun getOpenSession(): Flow<StockTakeSession?>
    suspend fun getOpenSessionOnce(): StockTakeSession?
    suspend fun startSession(startedBy: String): Long          // returns sessionId
    suspend fun closeSession(sessionId: Long)
    suspend fun cancelSession(sessionId: Long)
    fun getAllSessions(): Flow<List<StockTakeSession>>

    // Per-session item tracking
    fun getCountedProductIds(sessionId: Long): Flow<Set<Long>>
    fun getRecordsForSession(sessionId: Long): Flow<List<StockTake>>
    suspend fun upsertStockTakeRecord(stockTake: StockTake)    // save immediately on Check
}

class StockTakeRepositoryImpl @Inject constructor(
    private val stockTakeDao: StockTakeDao,
    private val sessionDao: StockTakeSessionDao,
    private val deviceSession: DeviceSession
) : StockTakeRepository {

    override fun getAllStockTakes(): Flow<List<StockTake>> = stockTakeDao.getAllStockTakes()

    override suspend fun insertStockTake(stockTake: StockTake) {
        withContext(Dispatchers.IO) {
            stockTakeDao.insertStockTake(
                stockTake.copy(deviceId = deviceSession.deviceId, storeId = deviceSession.storeId)
            )
        }
    }

    override fun getOpenSession(): Flow<StockTakeSession?> = sessionDao.getOpenSession()

    override suspend fun getOpenSessionOnce(): StockTakeSession? =
        withContext(Dispatchers.IO) { sessionDao.getOpenSessionOnce() }

    override suspend fun startSession(startedBy: String): Long =
        withContext(Dispatchers.IO) {
            val session = StockTakeSession(
                startedAt = System.currentTimeMillis(),
                startedBy = startedBy,
                storeId = deviceSession.storeId,
                deviceId = deviceSession.deviceId,
                status = StockTakeSessionStatus.OPEN.name
            )
            sessionDao.insertSession(session)
        }

    override suspend fun closeSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            val session = sessionDao.getSessionById(sessionId) ?: return@withContext
            sessionDao.updateSession(
                session.copy(
                    status = StockTakeSessionStatus.COMPLETED.name,
                    closedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun cancelSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            val session = sessionDao.getSessionById(sessionId) ?: return@withContext
            sessionDao.updateSession(
                session.copy(
                    status = StockTakeSessionStatus.CANCELLED.name,
                    closedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun getAllSessions(): Flow<List<StockTakeSession>> = sessionDao.getAllSessions()

    override fun getCountedProductIds(sessionId: Long): Flow<Set<Long>> =
        sessionDao.getCountedProductIds(sessionId).map { it.toSet() }

    override fun getRecordsForSession(sessionId: Long): Flow<List<StockTake>> =
        sessionDao.getRecordsForSession(sessionId)

    /**
     * Immediately persists a stock-take record (insert or replace).
     * Called on every "Check" tap — no buffering in memory.
     */
    override suspend fun upsertStockTakeRecord(stockTake: StockTake) {
        withContext(Dispatchers.IO) {
            stockTakeDao.insertStockTake(
                stockTake.copy(
                    deviceId = deviceSession.deviceId,
                    storeId = deviceSession.storeId
                )
            )
        }
    }
}
