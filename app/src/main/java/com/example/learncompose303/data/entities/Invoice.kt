package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val invoiceId: Long = 0,
    val customerId: Long,
    val totalAmount: Double,
    val invoiceDate: Long,  // Store as timestamp
    val dueDate: Long?,
    val status: String, // e.g., "Paid", "Pending", "Overdue"
    val currencyCode: String = "USD", // To support multi-currency
    val lastUpdated: Long? = null, // Timestamp of last update for auditing
    val isArchived: Boolean = false // To mark if the invoice is archived or not
)
