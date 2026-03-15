package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_list")
data class PriceList(
    @PrimaryKey(autoGenerate = true) val priceListId: Long = 0,
    var productId: Long,
    val productDescription: String,
    val unitCost: Double, // Cost per unit
    val sellingPrice: Double, // Selling price of the product
    val markupPercentage: Double, // Markup percentage
    val grossProfitPercentage: Double, // Gross profit percentage
    val isSynced: Boolean = false
)
