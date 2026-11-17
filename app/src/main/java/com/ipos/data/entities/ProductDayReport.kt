package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_day_reports")
data class ProductDayReport(
    @PrimaryKey(autoGenerate = true)
    val reportId: Long = 0,
    val productId: Long,
    val productName: String,
    val openingStock: Double,
    val closingStock: Double,
    val quantitySold: Int,
    val totalSales: Double,
    val totalCost: Double, // Sum of standard cost * quantity sold
    val totalProfit: Double, // (selling price - standard cost) * quantity sold
    val reportDate: Long, // Timestamp for the day's report
    val isSynced: Boolean = false
)
