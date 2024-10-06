package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.DayReports
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
}
