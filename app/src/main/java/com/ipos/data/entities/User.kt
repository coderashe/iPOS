package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [
        Index(value = ["userEmail"], unique = true), // Unique index for email
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val userFullName: String,
    val userContact: String,
    val userRole: String,  // e.g., cashier/businessman/admin
    val userPassword: String,
    val storeID: String,   // Store ID (now has a unique constraint)
    val userEmail: String,
    val userAddress: String?,
    val isSynced: Boolean = false
)
