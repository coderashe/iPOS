package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
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
    val amountTendered: Double?, // If applicable
    val changeBalance: Double?, // If applicable
    val invoiceDate: Long
)
