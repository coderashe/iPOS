package com.ipos.app.core

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TrialManager — Firestore-backed 7-day trial enforcement.
 *
 * Source of truth: Firestore collection "device_trials/{deviceId}"
 *   - trialStartDate (Long) : epoch millis when the device first launched
 *
 * Logic:
 *  1. On first launch → write trialStartDate = now to Firestore
 *  2. On subsequent launches (including after reinstall) → read from Firestore
 *  3. If elapsed days > TRIAL_DAYS → trial expired
 *
 * Offline: Firestore SDK caches locally, so after the first successful read/write
 * the check works offline too.
 */
@Singleton
class TrialManager @Inject constructor(
    private val deviceManager: DeviceManager,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "TrialManager"
        const val TRIAL_DAYS = 7L
        private const val COLLECTION = "device_trials"
        private const val FIELD_START = "trialStartDate"
        private const val FIELD_ACTIVATED = "isActivated"
    }

    sealed class TrialStatus {
        /** Still loading from Firestore */
        object Loading : TrialStatus()
        /** Trial is active — [daysLeft] days remaining (1..TRIAL_DAYS) */
        data class Active(val daysLeft: Long) : TrialStatus()
        /** Trial has expired */
        object Expired : TrialStatus()
    }

    /**
     * Check trial status for this device.
     * Suspends until Firestore responds (uses cached value when offline).
     */
    suspend fun checkTrialStatus(): TrialStatus {
        val deviceId = deviceManager.getDeviceId()
        return try {
            val docRef = firestore.collection(COLLECTION).document(deviceId)
            val snapshot = docRef.get().await()

            // Activated devices bypass the trial entirely
            if (snapshot.exists() && snapshot.getBoolean(FIELD_ACTIVATED) == true) {
                Log.d(TAG, "Device $deviceId is activated — full access granted")
                return TrialStatus.Active(daysLeft = Long.MAX_VALUE)
            }

            val trialStartDate: Long = if (snapshot.exists()) {
                // Device already registered — read existing start date
                snapshot.getLong(FIELD_START) ?: run {
                    val now = System.currentTimeMillis()
                    docRef.set(mapOf(FIELD_START to now, FIELD_ACTIVATED to false)).await()
                    now
                }
            } else {
                // First launch — record trial start date
                val now = System.currentTimeMillis()
                docRef.set(mapOf(FIELD_START to now, FIELD_ACTIVATED to false)).await()
                Log.d(TAG, "Trial started for device $deviceId at $now")
                now
            }

            val elapsedDays = elapsedDays(trialStartDate)
            val daysLeft = TRIAL_DAYS - elapsedDays

            Log.d(TAG, "Device $deviceId — elapsed: $elapsedDays days, left: $daysLeft days")

            if (daysLeft > 0) TrialStatus.Active(daysLeft) else TrialStatus.Expired
        } catch (e: Exception) {
            Log.e(TAG, "Firestore check failed: ${e.message}")
            // Fallback: allow access if we can't reach Firestore (avoids blocking offline users)
            TrialStatus.Active(daysLeft = TRIAL_DAYS)
        }
    }

    private fun elapsedDays(startMillis: Long): Long {
        val elapsed = System.currentTimeMillis() - startMillis
        return elapsed / (1000L * 60 * 60 * 24)
    }
}
