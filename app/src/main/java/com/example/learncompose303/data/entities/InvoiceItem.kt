package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoiceitem")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val customerId: Long,
    val quantity: Int,
    val price: Double, // Price per unit
    val total: Double, // quantity * price
    val isSynced: Boolean = false
)
