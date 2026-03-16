package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discountsreceived")
data class DiscountsReceived(
    @PrimaryKey(autoGenerate = true)
    val discountId: Long,
    val orderId: Long,
    val discountAmount: Double,
    val dateReceived: Long, // Date when the discount was received
    val deviceId: String = "",
    val storeId: String = "",
    val isSynced: Boolean = false
)
