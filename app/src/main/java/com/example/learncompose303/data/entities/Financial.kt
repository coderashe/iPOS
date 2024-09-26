package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial")
data class Financial(
    @PrimaryKey(autoGenerate = true) val financialId: Long = 0,
    val currencyCode: String, // E.g., USD, ZWG, RAND
    val currencyDescription: String,
    val isBaseCurrency: Boolean, // To indicate the base currency
    val exchangeRate: Double, // Exchange rate for the currency
    val taxCode: String?, // E.g., VAT, EX
    val taxDescription: String?,
    val taxPercentage: Double?, // Percentage of the tax
    val paymentType: String? // Cash, Swipe, EcoCash, etc.
)
