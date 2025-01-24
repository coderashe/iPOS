package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Inventory(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val productDescription: String,
    val currentStockLevel: Double,
    val saleable: Boolean, // Determines if the product can be sold
    val purchasable: Boolean, // Determines if the product can be purchased
    val averageCost: Double, // Average cost of the product
    val standardCost: Double, // Standard cost used for cost comparison
    val salesTaxCode: String?, // Tax code for sales
    val purchasableTaxCode: String?, // Tax code for purchases
    val inclusiveTaxed: Boolean, // Whether the price is inclusive of tax
    val imageUri: String? = null
)
