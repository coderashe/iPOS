package com.ipos.app.core

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeviceManager - Manages device identification and metadata for multi-device tracking
 * 
 * Purpose: Uniquely identify each POS device to track which device created/modified data
 * This enables device-level reporting and analytics on the dashboard.
 * 
 * Device ID Source: Android ANDROID_ID (Settings.Secure.ANDROID_ID)
 * - Unique per device
 * - Survives app reinstall
 * - No permissions required
 * - Resets only on factory reset
 * 
 * @property context Application context
 */
@Singleton
class DeviceManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "DeviceManager"
        private const val PREFS_NAME = "device_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_STORE_ID = "store_id"
        private const val KEY_DEVICE_REGISTERED = "device_registered"
        private const val KEY_REGISTRATION_DATE = "registration_date"
        private const val DEFAULT_DEVICE_NAME = "POS Device"
    }

    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the unique device ID (Android ANDROID_ID)
     * This ID is stable across app reinstalls but changes on factory reset
     */
    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            // Generate device ID from Android ANDROID_ID
            deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: generateFallbackId()
            
            // Store for quick access
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            Log.d(TAG, "Device ID generated and stored: $deviceId")
        }
        
        return deviceId
    }

    /**
     * Get the user-friendly device name
     * Default: "POS Device" until user sets a custom name
     */
    fun getDeviceName(): String {
        return prefs.getString(KEY_DEVICE_NAME, DEFAULT_DEVICE_NAME) ?: DEFAULT_DEVICE_NAME
    }

    /**
     * Set a custom device name (e.g., "Main Counter", "Mobile POS 1")
     * This helps users identify devices in reports
     */
    fun setDeviceName(name: String) {
        prefs.edit().putString(KEY_DEVICE_NAME, name).apply()
        Log.d(TAG, "Device name updated: $name")
    }

    /**
     * Get the store ID associated with this device
     */
    fun getStoreId(): String {
        return prefs.getString(KEY_STORE_ID, "") ?: ""
    }

    /**
     * Set the store ID associated with this device
     */
    fun setStoreId(storeId: String) {
        val trimmed = storeId.trim()
        prefs.edit().putString(KEY_STORE_ID, trimmed).apply()
        Log.d(TAG, "Store ID updated: $trimmed")
    }

    /**
     * Check if device has been registered with the server
     */
    fun isDeviceRegistered(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_REGISTERED, false)
    }

    /**
     * Mark device as registered after successful server registration
     */
    fun markDeviceAsRegistered() {
        prefs.edit()
            .putBoolean(KEY_DEVICE_REGISTERED, true)
            .putLong(KEY_REGISTRATION_DATE, System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Device marked as registered")
    }

    /**
     * Get device registration date (timestamp)
     */
    fun getRegistrationDate(): Long {
        return prefs.getLong(KEY_REGISTRATION_DATE, 0L)
    }

    /**
     * Get complete device info for logging/debugging
     */
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = getDeviceId(),
            deviceName = getDeviceName(),
            isRegistered = isDeviceRegistered(),
            registrationDate = getRegistrationDate(),
            androidVersion = android.os.Build.VERSION.SDK_INT,
            deviceModel = android.os.Build.MODEL,
            manufacturer = android.os.Build.MANUFACTURER
        )
    }

    /**
     * Reset device registration (for testing or device transfer)
     * WARNING: This will cause the device to re-register with server
     */
    fun resetDeviceRegistration() {
        prefs.edit()
            .putBoolean(KEY_DEVICE_REGISTERED, false)
            .remove(KEY_REGISTRATION_DATE)
            .apply()
        Log.w(TAG, "Device registration reset")
    }

    /**
     * Fallback ID generation if ANDROID_ID is unavailable
     * Uses timestamp + random to ensure uniqueness
     */
    private fun generateFallbackId(): String {
        val fallbackId = "FALLBACK_${System.currentTimeMillis()}_${(0..9999).random()}"
        Log.w(TAG, "ANDROID_ID unavailable, using fallback: $fallbackId")
        return fallbackId
    }

    /**
     * Log device information for debugging
     */
    fun logDeviceInfo() {
        val info = getDeviceInfo()
        Log.d(TAG, """
            ========== Device Information ==========
            Device ID: ${info.deviceId}
            Device Name: ${info.deviceName}
            Registered: ${info.isRegistered}
            Registration Date: ${info.registrationDate}
            Android Version: ${info.androidVersion}
            Model: ${info.deviceModel}
            Manufacturer: ${info.manufacturer}
            ========================================
        """.trimIndent())
    }
}

/**
 * Data class containing complete device information
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val isRegistered: Boolean,
    val registrationDate: Long,
    val androidVersion: Int,
    val deviceModel: String,
    val manufacturer: String
)
