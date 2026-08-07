package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.learncompose303.data.entities.TradeReceivables

@Dao
interface TradeReceivablesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradeReceivable(tradeReceivable: TradeReceivables)

    @Update
    suspend fun updateTradeReceivable(tradeReceivable: TradeReceivables)

    @Delete
    suspend fun deleteTradeReceivable(tradeReceivable: TradeReceivables)

    @Query("SELECT * FROM tradereceivables WHERE receivableId = :id")
    suspend fun getTradeReceivableById(id: Long): TradeReceivables?

    @Query("SELECT * FROM tradereceivables ORDER BY dueDate ASC")
    fun getAllTradeReceivables(): Flow<List<TradeReceivables>>

    @Query("SELECT * FROM tradereceivables WHERE dueDate <= :currentTime")
    fun getOverdueReceivables(currentTime: Long): Flow<List<TradeReceivables>>

    @Query("SELECT * FROM tradereceivables WHERE isSynced = 0 AND storeId != '' AND storeId IS NOT NULL LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<TradeReceivables>

    @Query("UPDATE tradereceivables SET isSynced = 1 WHERE receivableId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM tradereceivables WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE tradereceivables SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)
}
