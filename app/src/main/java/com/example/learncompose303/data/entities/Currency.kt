package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency(
    @PrimaryKey val code: String,  // e.g., "USD", "ZAR", "ZiG", "Ecocash"
    val name: String,              // Full name e.g., "South African Rand"
    val rateToBase: Double         // Exchange rate to USD
)
