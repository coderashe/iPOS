package com.example.learncompose303.data

data class FailedInvoiceItem(
    val rowIndex: Int,
    var brand: String,
    var size: Double,
    var description: String,
    val quantity: Int,
    val amountTendered: Double,
    val customerId: Long,
    val failureReason: String
)
