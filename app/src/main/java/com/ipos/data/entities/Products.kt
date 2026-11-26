package com.ipos.data.entities

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
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ShoeSize::class,
            parentColumns = ["shoeSizeId"],
            childColumns = ["shoeSizeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["brandID"]),
        Index(value = ["shoeSizeId"]),
        Index(value = ["productCode"], unique = true)
    ]
)
data class Products(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val productDescription: String,
    val productQty: Long,
    // Nullable — only used for SHOES business type
    val brandID: Long? = null,
    val shoeSizeId: Long? = null,
    val saleable: Boolean,
    val purchasable: Boolean,
    val averageCost: Double,
    val standardCost: Double,
    val salesTaxCode: String?,
    val productCode: String,
    val inclusiveTaxed: Boolean,
    val imageUri: String? = null,
    val lastUpdated: Long? = null,
    val isSynced: Boolean = false,
    // Multi-business fields (nullable for backward compatibility)
    val category: String? = null,   // e.g. "Food", "Drinks", "Beef", "Chicken"
    val unit: String? = null,       // e.g. "each", "kg", "g"
    val stockUnit: String? = null   // "count" or "weight" — drives butcher cart input
)

