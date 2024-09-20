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
    val status: String // e.g., "Paid", "Pending", "Overdue"
)
