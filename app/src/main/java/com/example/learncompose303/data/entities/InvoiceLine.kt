package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_lines")
data class InvoiceLine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val invoiceID: Long,
    val invoiceStatus: String,
    val productID: Long,
    val productName: String,
    val productPrice: Double,
    val productQtySold: Int,
    val customerID: Long,
    val customerName: String,
    val customerPhone: String?,
    val totalAmount: Double,
    val amountTendered: Double?,
    val changeBalance: Double?,
    val invoiceDate: Long,
    val taxAmount: Double? = null, // Tax amount for this invoice line
    val discountAmount: Double? = null, // Discount applied to the line item
    val createdAt: Long = System.currentTimeMillis(), // Created timestamp for auditing
    val updatedAt: Long? = null // Last update timestamp for auditing
)
