package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failed_invoices")
data class FailedInvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rowIndex: Int,
    var brand: String,
    var size: Double,
    var description: String,
    val quantity: Int,
    val amountTendered: Double,
    val customerId: Long,
    val failureReason: String,
    val suggestedMatches: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
