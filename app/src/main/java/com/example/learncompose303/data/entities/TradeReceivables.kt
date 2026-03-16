package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tradereceivables")
data class TradeReceivables(
    @PrimaryKey(autoGenerate = true)
    val receivableId: Long,
    val orderId: Long,
    val totalAmount: Double,
    val amountPaid: Double,
    val creditPeriod: Int, // in days
    val dueDate: Long, // calculated based on the creditPeriod
    val paymentStatus: String, // e.g., "Unpaid", "Partial", "Paid"
    val dateCreated: Long,
    val deviceId: String = "",
    val storeId: String = "",
    val isSynced: Boolean = false
)
