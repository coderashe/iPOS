package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [
        Index(value = ["userEmail"], unique = true),
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val userFullName: String,
    val userContact: String,
    val userRole: String,
    val userPassword: String,
    val storeID: String,
    val userEmail: String,
    val userAddress: String?,
    val isSynced: Boolean = false,
    val profileImageUri: String? = null  // local URI to profile photo
)
