package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val supplierId: Long,
    val name: String,
    val location: String,
    val phoneNumber: String,
    val email: String?,
    val productIdsSupplied: List<Long>, // List of products supplied
    val rating: Int?, // Optional rating out of 5
    val comment: String?,
    val offerDiscount: Boolean,
    val offerCredit: Boolean
)
