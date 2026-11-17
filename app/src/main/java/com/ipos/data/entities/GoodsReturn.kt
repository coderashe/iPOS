package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goods_return")
data class GoodsReturn(
    @PrimaryKey(autoGenerate = true)
    val returnId: Long = 0,
    val supplierId: Long,
    val productId: Long,
    val qtyReturned: Double,
    val reason: String,
    val returnDate: Long
)
