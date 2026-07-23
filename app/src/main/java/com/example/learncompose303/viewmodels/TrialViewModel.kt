package com.example.learncompose303.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.app.core.TrialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrialViewModel @Inject constructor(
    private val trialManager: TrialManager,
    deviceManager: com.example.learncompose303.app.core.DeviceManager
) : ViewModel() {

    /** Exposed so the expired screen can show it to the user */
    val deviceId: String = deviceManager.getDeviceId()

    private val _trialStatus = MutableStateFlow<TrialManager.TrialStatus>(TrialManager.TrialStatus.Loading)
    val trialStatus: StateFlow<TrialManager.TrialStatus> = _trialStatus

    init {
        checkTrial()
    }

    fun checkTrial() {
        viewModelScope.launch {
            _trialStatus.value = TrialManager.TrialStatus.Loading
            _trialStatus.value = trialManager.checkTrialStatus()
        }
    }
}
