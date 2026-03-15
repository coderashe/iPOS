package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val expenseId: Long = 0,
    val description: String,
    val amount: Double,
    val expenseDate: Long,
    val category: String, // e.g., Rent, Wages, Sundry, etc.
    val isSynced: Boolean = false
)
