package com.example.learncompose303.data.entities

data class ProductCsvModel(
    val productDescription: String,
    val currentStockLevel: Double,
    val saleable: Boolean,
    val purchasable: Boolean,
    val averageCost: Double,
    val standardCost: Double,
    val salesTaxCode: String?,
    val purchasableTaxCode: String?,
    val inclusiveTaxed: Boolean,
    val imageResId: Int?
)

