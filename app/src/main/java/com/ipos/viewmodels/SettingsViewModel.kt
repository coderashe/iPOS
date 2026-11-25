package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import com.ipos.app.core.BusinessType
import com.ipos.data.repositories.BusinessTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin ViewModel wrapper over the singleton BusinessTypeRepository.
 * Every screen that creates its own hiltViewModel<SettingsViewModel>()
 * still reads from the same underlying StateFlow — no stale reads.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: BusinessTypeRepository
) : ViewModel() {

    val businessType: StateFlow<BusinessType?> = repository.businessType

    fun saveBusinessType(type: BusinessType) = repository.save(type)

    fun isOnboardingComplete(): Boolean = repository.isOnboardingComplete()
}
