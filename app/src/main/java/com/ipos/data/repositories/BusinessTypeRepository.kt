package com.ipos.data.repositories

import android.content.Context
import com.ipos.app.core.BusinessType
import com.ipos.data.data_source.AppSettingsDao
import com.ipos.data.entities.AppSettings
import com.ipos.data.entities.AppSettingsKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "ipos_prefs"

/**
 * Singleton source of truth for business type.
 * All ViewModels and screens read from this — no fragmented instances.
 */
@Singleton
class BusinessTypeRepository @Inject constructor(
    private val appSettingsDao: AppSettingsDao,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Read synchronously at construction — SharedPreferences is always synchronous
    private val _businessType = MutableStateFlow(readFromPrefs())
    val businessType: StateFlow<BusinessType?> = _businessType.asStateFlow()

    private fun readFromPrefs(): BusinessType? {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(AppSettingsKeys.BUSINESS_TYPE, null)
        return raw?.let { runCatching { BusinessType.valueOf(it) }.getOrNull() }
    }

    fun save(type: BusinessType) {
        // 1. In-memory — instant, visible to all observers immediately
        _businessType.value = type

        // 2. SharedPreferences — synchronous commit so it survives process restart
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(AppSettingsKeys.BUSINESS_TYPE, type.name)
            .commit()

        // 3. Room — durable background persist
        scope.launch {
            runCatching {
                appSettingsDao.setValue(
                    AppSettings(key = AppSettingsKeys.BUSINESS_TYPE, value = type.name)
                )
            }
        }
    }

    fun isOnboardingComplete(): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .contains(AppSettingsKeys.BUSINESS_TYPE)
}
