package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val productId: Long = 0,
    val name: String,
    val price: Double,
    val imageResId: Int? = null,
//    val totalStockLevel: Double // New field for tracking stock levels
)

