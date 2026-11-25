package com.ipos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Generic key/value settings table.
 * Use [AppSettingsKeys] constants for keys to avoid magic strings.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val key: String,
    val value: String
)

object AppSettingsKeys {
    const val BUSINESS_TYPE = "business_type"
}
