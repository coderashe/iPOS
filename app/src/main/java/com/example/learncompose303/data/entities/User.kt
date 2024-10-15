package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val userFullName: String,
    val userContact: String,
    val userRole: String,  // cashier/businessman/admin
    val userPassword: String,
    val storeID: String,
    val userEmail: String,  // New field
    val userAddress: String? // New field, optional
)
