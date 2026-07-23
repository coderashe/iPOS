package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StockTakeSessionStatus { OPEN, COMPLETED, CANCELLED }

@Entity(tableName = "stock_take_session")
data class StockTakeSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val startedAt: Long,
    val closedAt: Long? = null,
    val status: String = StockTakeSessionStatus.OPEN.name, // stored as String for Room
    val startedBy: String,
    val storeId: String = "",
    val deviceId: String = ""
)
