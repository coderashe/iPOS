package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long,
    val supplierId: Long,
    val orderDate: Long,
    val totalAmount: Double,
    val status: String, // "Created", "Processed", "Received", "WaitingForPayment", "Completed"
    val paymentMethod: String?, // "Cash" or "Credit", null if not yet confirmed
    val discount: Double?, // Optional discount applied after receiving the products
    val receivedDate: Long?, // Date when products are received
    val productDetails: List<ProductOrderDetails> // List of products for the order
)

data class ProductOrderDetails(
    val productId: Long,
    val quantity: Double,
    val unitPrice: Double
)

