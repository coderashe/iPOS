package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val customerId: Long = 0,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val customerType: String = "Regular", // e.g., Regular, VIP, Wholesale
    val taxIdentificationNumber: String? = null, // For business customers with tax requirements
    val loyaltyPoints: Int = 0 // For loyalty programs, if applicable
)
