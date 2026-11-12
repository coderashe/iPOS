package com.ipos.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ipos.data.entities.DayReports
import kotlinx.coroutines.flow.Flow

@Dao
interface DayReportsDao {

    @Query("SELECT * FROM day_reports WHERE reportId = :id")
    suspend fun getDayReportById(id: Long): DayReports?

    @Query("SELECT * FROM day_reports")
    fun getAllDayReports(): Flow<List<DayReports>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayReport(dayReport: DayReports): Long

    @Update
    suspend fun updateDayReport(dayReport: DayReports)

    @Delete
    suspend fun deleteDayReport(dayReport: DayReports)

    @Query("SELECT * FROM day_reports WHERE isSynced = 0 AND storeId != '' AND storeId IS NOT NULL LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<DayReports>

    @Query("UPDATE day_reports SET isSynced = 1 WHERE reportId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM day_reports WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE day_reports SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)
}
