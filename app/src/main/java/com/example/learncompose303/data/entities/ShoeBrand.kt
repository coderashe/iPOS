package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shoeBrand")
data class ShoeBrand(
    @PrimaryKey(autoGenerate = true)
    val brandID: Long = 0,
    val brandName: String // e.g., "Nike", "Puma", "Vans"
)
