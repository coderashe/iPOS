package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "shoe_sizes",
    foreignKeys = [
        ForeignKey(
            entity = ShoeBrand::class,
            parentColumns = ["brandID"],
            childColumns = ["brandID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["brandID"])]
)
data class ShoeSize(
    @PrimaryKey(autoGenerate = true)
    val shoeSizeId: Long = 0,
    val brandID: Long, // Foreign key referencing Label
    val size: Double, // Shoe size (e.g., 1-11)
    val quantity: Long, // Quantity available for this size
    val isSynced: Boolean = false
)
