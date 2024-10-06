package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_reports")
data class DayReports(
    @PrimaryKey(autoGenerate = true) val reportId: Long = 0,
    val totalInvoices: Int,
    val totalOrders: Int,
    val totalProductsSold: Int,
    val totalInvoiceSales: Double,
    val reportDate: Long, // Timestamp for the day's report
    val startTime: Long,
    val endTime: Long
)

