package com.example.learncompose303.data

// Data class for CSV rows (same as before)
data class CsvInvoiceItem(
    val brand: String,
    val size: Double,
    val description: String,
    val quantity: Int,
    val amountTendered: Double,
    val customerId: Long
)
