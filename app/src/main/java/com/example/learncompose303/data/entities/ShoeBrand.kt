package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class ShoeBrand(
    @PrimaryKey(autoGenerate = true)
    val brandID: Long = 0,
    val labelName: String // e.g., "Nike", "Puma", "Vans"
)
