package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.app.usecases.FiscalizeInvoiceUseCase
import com.ipos.data.entities.FiscalInvoiceRequest
import com.ipos.network.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject


@HiltViewModel
class FiscalizationViewModel @Inject constructor(
    private val fiscalizeInvoiceUseCase: FiscalizeInvoiceUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    private val _fiscalizationResult = MutableStateFlow<Result<JsonObject>?>(null)
    val fiscalizationResult: StateFlow<Result<JsonObject>?> = _fiscalizationResult

    fun fiscalizeInvoice(invoiceRequest: FiscalInvoiceRequest) {
        viewModelScope.launch {
            _fiscalizationResult.value = fiscalizeInvoiceUseCase(invoiceRequest)
        }
    }
}