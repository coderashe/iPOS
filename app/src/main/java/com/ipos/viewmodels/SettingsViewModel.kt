package com.ipos.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.app.core.BusinessType
import com.ipos.data.data_source.AppSettingsDao
import com.ipos.data.entities.AppSettings
import com.ipos.data.entities.AppSettingsKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PREFS_NAME = "ipos_prefs"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsDao: AppSettingsDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Read synchronously on construction — SharedPreferences.getString() is always synchronous,
    // so there's no null window between init and the first composition frame.
    private val _businessType = MutableStateFlow(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(AppSettingsKeys.BUSINESS_TYPE, null)
            ?.let { runCatching { BusinessType.valueOf(it) }.getOrNull() }
    )
    val businessType: StateFlow<BusinessType?> = _businessType.asStateFlow()

    init { /* nothing async needed */ }

    /**
     * Called once from BusinessTypeScreen. Persists to both SharedPreferences (fast reads)
     * and the app_settings Room table (survives backup/restore and future sync).
     *
     * StateFlow and SharedPreferences are updated SYNCHRONOUSLY on the caller's thread
     * so that subsequent screens and StateFlow observers immediately see the updated type,
     * even before any background coroutine completes.
     */
    fun saveBusinessType(type: BusinessType) {
        // 1. Immediately update in-memory state
        _businessType.value = type

        // 2. Synchronous commit to SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(AppSettingsKeys.BUSINESS_TYPE, type.name)
            .commit()

        // 3. Durable, syncable Room persist in background
        viewModelScope.launch {
            try {
                appSettingsDao.setValue(AppSettings(key = AppSettingsKeys.BUSINESS_TYPE, value = type.name))
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to save business_type to Room: ${e.message}", e)
            }
        }
    }

    /** True if business type has been chosen (app is past onboarding). */
    fun isOnboardingComplete(): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .contains(AppSettingsKeys.BUSINESS_TYPE)
}
