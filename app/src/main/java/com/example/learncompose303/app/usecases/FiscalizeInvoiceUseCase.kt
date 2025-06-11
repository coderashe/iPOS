package com.example.learncompose303.app.usecases

import com.example.learncompose303.data.entities.FiscalInvoiceRequest
import com.example.learncompose303.data.repositories.FiscalizationRepository
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class FiscalizeInvoiceUseCase @Inject constructor(
    private val repository: FiscalizationRepository
) {
    suspend operator fun invoke(invoiceRequest: FiscalInvoiceRequest): Result<JsonObject> {
        return repository.fiscalizeInvoice(invoiceRequest)
    }
}