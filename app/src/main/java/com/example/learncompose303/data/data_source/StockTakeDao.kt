package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.learncompose303.data.entities.StockTake
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTake(stockTake: StockTake)

    @Query("SELECT * FROM stock_take")
    fun getAllStockTakes(): Flow<List<StockTake>>

    @Query("SELECT * FROM stock_take WHERE isSynced = 0 AND storeId != '' AND storeId IS NOT NULL LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<StockTake>

    @Query("UPDATE stock_take SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)

    @Query("SELECT COUNT(*) FROM stock_take WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE stock_take SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)

    /** Returns the single stock-take record for a product in a given session, or null. */
    @Query("SELECT * FROM stock_take WHERE sessionId = :sessionId AND productId = :productId LIMIT 1")
    suspend fun getRecordForProduct(sessionId: Long, productId: Long): StockTake?

    /** Update sessionId column (used for backfill of legacy 0-session records). */
    @Query("UPDATE stock_take SET sessionId = :sessionId WHERE id = :id")
    suspend fun updateSessionId(id: Int, sessionId: Long)
}