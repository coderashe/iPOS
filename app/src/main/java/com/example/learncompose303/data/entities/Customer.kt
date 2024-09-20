package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer")
data class Customer(
    @PrimaryKey(autoGenerate = true) val customerId: Long = 0,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?
)
