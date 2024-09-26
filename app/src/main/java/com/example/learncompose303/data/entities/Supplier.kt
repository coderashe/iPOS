package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val vendorId: Long = 0,
    val description: String,
    val productGroupId: Long // Link to product groups
)
