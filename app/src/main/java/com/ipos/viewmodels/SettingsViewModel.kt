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
     */
    fun saveBusinessType(type: BusinessType) {
        viewModelScope.launch {
            // SharedPreferences — instant read on next launch
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(AppSettingsKeys.BUSINESS_TYPE, type.name)
                .apply()

            // Room — durable, syncable
            appSettingsDao.setValue(AppSettings(key = AppSettingsKeys.BUSINESS_TYPE, value = type.name))

            _businessType.value = type
        }
    }

    /** True if business type has been chosen (app is past onboarding). */
    fun isOnboardingComplete(): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .contains(AppSettingsKeys.BUSINESS_TYPE)
}
