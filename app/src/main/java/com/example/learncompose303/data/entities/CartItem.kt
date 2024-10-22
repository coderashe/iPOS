package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["customerId"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Inventory::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["invoiceId"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartItemId: Long = 0,
    val customerId: Long,
    val productId: Long,
    val invoiceId: Long,
    val totalAmount: Double,
    val totalAmountTendered: Double,
    val change: Double,
    val taxAmount: Double? = null, // Tax amount on this cart item (optional)
    val discountAmount: Double? = null, // Discounts applied to the item
    val createdAt: Long = System.currentTimeMillis(), // Track when the item was added to the cart
    val updatedAt: Long? = null // Track when the item was last updated
)
