package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    val supplierId: Long,
    val productId: Long,
    val qtyOrdered: Int,
    val costPrice: Double,
    val orderDate: Long
)
