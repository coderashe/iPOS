package com.example.learncompose303.data.entities

data class PriceListCsvModel(
    val productId: Long,
    val productDescription: String,
    val unitCost: Double,
    val sellingPrice: Double,
    val markupPercentage: Double,
    val grossProfitPercentage: Double
)
