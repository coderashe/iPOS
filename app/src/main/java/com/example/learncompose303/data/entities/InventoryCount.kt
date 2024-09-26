package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_count")
data class InventoryCount(
    @PrimaryKey(autoGenerate = true)
    val countId: Long = 0,
    val productId: Long,
    val systemQty: Int,
    val countedQty: Int,
    val dripLoss: Int?, // Loss from handling/storage
    val variance: Int, // Difference between system and counted quantity
    val notes: String
)
