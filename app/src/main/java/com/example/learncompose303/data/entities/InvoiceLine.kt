package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_lines")
data class InvoiceLine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceId: Long,
    val invoiceStatus: String,
    val productID: Long,
    val productName: String,
    val productPrice: Double,
    val productQtySold: Int,
    val cashier: String,
    val customerID: Long,
    val customerName: String,
    val customerPhone: String?,
    val totalAmount: Double,
    val amountTendered: Double?,
    val changeBalance: Double?,
    val invoiceDate: Long,
    val taxAmount: Double? = null,
    val currentStock: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val synced: Boolean = false,
    val deviceId: String? = null,
    val storeId: String? = null
)
