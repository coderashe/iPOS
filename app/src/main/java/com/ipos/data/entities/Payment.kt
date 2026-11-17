package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment")
data class Payment(
    @PrimaryKey(autoGenerate = true) val paymentId: Long = 0,
    val invoiceId: Long,
    val paymentDate: Long,  // Store as timestamp
    val amount: Double,
    val method: String, // e.g., "Credit Card", "Cash", "Bank Transfer"
    val deviceId: String = "",
    val storeId: String = "",
    val isSynced: Boolean = false
)
