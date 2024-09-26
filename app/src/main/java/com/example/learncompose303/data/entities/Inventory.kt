package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey(autoGenerate = true) val inventoryId: Long = 0,
    val productId: Long,
    val qty: Int,
    val costPrice: Double, // Cost at which the product was acquired
    val sellingPrice: Double // Selling price of the product
)
