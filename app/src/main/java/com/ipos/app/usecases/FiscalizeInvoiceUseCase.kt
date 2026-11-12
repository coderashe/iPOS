package com.ipos.app.usecases

import com.ipos.data.entities.FiscalInvoiceRequest
import com.ipos.data.repositories.FiscalizationRepository
import com.ipos.network.FiscalizationApi
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class FiscalizeInvoiceUseCase @Inject constructor(
    private val api: FiscalizationApi
) {
    suspend operator fun invoke(request: FiscalInvoiceRequest): Result<JsonObject> {
        return try {
            val response = api.fiscalizeInvoice(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
