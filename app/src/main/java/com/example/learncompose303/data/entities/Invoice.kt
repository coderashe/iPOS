package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val invoiceId: Long = 0,
    val customerId: Long,
    val totalAmount: Double,
    val invoiceDate: Long,
    val dueDate: Long?,
    val status: String,
    val currencyCode: String,
    val lastUpdated: Long? = null,
    val isArchived: Boolean = false
)

data class InvoiceWithCurrency(
    val totalAmount: Double,
    val currencyCode: String
)

data class MonthlySalesWithCurrency(
    val monthNumber: String,
    val totalAmount: Double,
    val currencyCode: String
)


fun mapInvoiceToFirestore(invoice: Invoice): Map<String, Any?> {
    return hashMapOf(
        "invoiceId" to invoice.invoiceId,
        "customerId" to invoice.customerId,
        "totalAmount" to invoice.totalAmount,
        "invoiceDate" to invoice.invoiceDate,
        "dueDate" to invoice.dueDate,
        "status" to invoice.status,
        "currencyCode" to invoice.currencyCode,
        "lastUpdated" to invoice.lastUpdated,
        "isArchived" to invoice.isArchived
    )
}

