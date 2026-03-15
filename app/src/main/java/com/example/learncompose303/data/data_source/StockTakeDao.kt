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

    @Query("SELECT * FROM stock_take WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<StockTake>

    @Query("UPDATE stock_take SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)

    @Query("SELECT COUNT(*) FROM stock_take WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}