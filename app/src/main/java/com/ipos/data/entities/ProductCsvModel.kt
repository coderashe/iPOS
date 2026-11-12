package com.ipos.data.entities

data class ProductCsvModel(
    val productDescription: String,
    val productQty: Long,
    val brandID: Long,
    val shoeSizeId: Long,
    val saleable: Boolean,
    val purchasable: Boolean,
    val averageCost: Double,
    val standardCost: Double,
    val salesTaxCode: String?,
    val productCode: String,
    val inclusiveTaxed: Boolean,
    val imageUri: String?,
    val lastUpdated: Long? = null
)
