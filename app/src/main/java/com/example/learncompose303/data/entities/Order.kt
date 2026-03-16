package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val quantity: Int,
    val dueDate: String, // Store as a String for simplicity (format: "dd/MM/yyyy")
    val notes: String? = null,
    val orderDate: Long,
    val deviceId: String = "",
    val storeId: String = ""
)
