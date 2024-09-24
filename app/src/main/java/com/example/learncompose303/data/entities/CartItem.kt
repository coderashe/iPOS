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
            entity = Product::class,
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
    val change: Double
)
