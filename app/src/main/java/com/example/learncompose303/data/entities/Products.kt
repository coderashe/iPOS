package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = ShoeBrand::class,
            parentColumns = ["brandID"],
            childColumns = ["brandID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShoeSize::class,
            parentColumns = ["shoeSizeId"],
            childColumns = ["shoeSizeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["brandID"]),
        Index(value = ["shoeSizeId"]),
        Index(value = ["productCode"], unique = true) // Ensure productCode is unique
    ]
)
data class Products(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val productDescription: String, // e.g., "Nike Air Max Size 10"
    val productQty: Long,
    val brandID: Long, // Foreign key linking to Label
    val shoeSizeId: Long, // Foreign key linking to ShoeSize
    val saleable: Boolean, // Indicates if the product can be sold
    val purchasable: Boolean, // Indicates if the product can be purchased
    val averageCost: Double, // Average cost of the product
    val standardCost: Double, // Standard cost of the product
    val salesTaxCode: String?, // Tax code for sales
    val productCode: String, // Unique product code (storeID + productId)
    val inclusiveTaxed: Boolean, // Indicates if the price is inclusive of tax
    val imageUri: String? = null // Optional URI for product image
)

