package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.DayReportsDao
import com.example.learncompose303.data.entities.DayReports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface DayReportsRepository {
    suspend fun getDayReportById(id: Long): DayReports?
    fun getAllDayReports(): Flow<List<DayReports>>
    suspend fun insertDayReport(dayReport: DayReports): Long
    suspend fun updateDayReport(dayReport: DayReports)
    suspend fun deleteDayReport(dayReport: DayReports)
}

class DayReportsRepositoryImpl @Inject constructor(
    private val dayReportsDao: DayReportsDao
) : DayReportsRepository {

    override suspend fun getDayReportById(id: Long): DayReports? {
        return withContext(Dispatchers.IO) {
            dayReportsDao.getDayReportById(id)
        }
    }

    override fun getAllDayReports(): Flow<List<DayReports>> {
        return dayReportsDao.getAllDayReports()
    }

    override suspend fun insertDayReport(dayReport: DayReports): Long {
        return withContext(Dispatchers.IO) {
            dayReportsDao.insertDayReport(dayReport)
        }
    }

    override suspend fun updateDayReport(dayReport: DayReports) {
        return withContext(Dispatchers.IO) {
            dayReportsDao.updateDayReport(dayReport)
        }
    }

    override suspend fun deleteDayReport(dayReport: DayReports) {
        return withContext(Dispatchers.IO) {
            dayReportsDao.deleteDayReport(dayReport)
        }
    }
}
