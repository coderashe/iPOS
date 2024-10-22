package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// This entity will track the cash inflow and outflow for purchases made with cash.
@Entity(tableName = "cashflow")
data class CashFlow(
    @PrimaryKey(autoGenerate = true)
    val cashFlowId: Long,
    val orderId: Long,
    val amount: Double,
    val cashOutflow: Boolean, // true if outflow (payment), false if inflow (refund or correction)
    val transactionDate: Long,
    val description: String?
)
