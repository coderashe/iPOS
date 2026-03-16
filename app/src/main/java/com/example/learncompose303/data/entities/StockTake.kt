package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stock_take")
data class StockTake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Long,
    val recordedStock: Long,
    val countedStock: Double,
    val variance: Double,
    val varianceReason: String?,
    val dateRecorded: Long,
    val recordedBy: String,
    val deviceId: String = "",
    val storeId: String = "",
    val isSynced: Boolean = false
)
