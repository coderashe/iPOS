package com.example.learncompose303.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_info")
data class DeviceInfo(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val storeId: String,
    val lastSeen: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
