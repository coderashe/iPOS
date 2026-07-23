package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.StockTakeSession
import com.example.learncompose303.data.entities.StockTakeSessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTakeSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StockTakeSession): Long

    @Update
    suspend fun updateSession(session: StockTakeSession)

    /** Returns the currently open session, or null if none. */
    @Query("SELECT * FROM stock_take_session WHERE status = 'OPEN' ORDER BY startedAt DESC LIMIT 1")
    fun getOpenSession(): Flow<StockTakeSession?>

    /** One-shot fetch of the open session (for coroutines). */
    @Query("SELECT * FROM stock_take_session WHERE status = 'OPEN' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getOpenSessionOnce(): StockTakeSession?

    @Query("SELECT * FROM stock_take_session ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<StockTakeSession>>

    @Query("SELECT * FROM stock_take_session WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): StockTakeSession?

    /** IDs of products already counted in a given session. */
    @Query("SELECT DISTINCT productId FROM stock_take WHERE sessionId = :sessionId")
    fun getCountedProductIds(sessionId: Long): Flow<List<Long>>

    /** All stock-take records for a specific session. */
    @Query("SELECT * FROM stock_take WHERE sessionId = :sessionId")
    fun getRecordsForSession(sessionId: Long): Flow<List<com.example.learncompose303.data.entities.StockTake>>
}
