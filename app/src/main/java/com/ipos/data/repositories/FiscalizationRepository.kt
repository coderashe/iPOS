package com.ipos.data.repositories

import com.ipos.data.entities.FiscalInvoiceRequest
import com.ipos.network.ErrorHandler
import com.ipos.network.FiscalizationApi
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class FiscalizationRepository @Inject constructor(
    private val api: FiscalizationApi,
    private val errorHandler: ErrorHandler
) {
    suspend fun fiscalizeInvoice(invoiceRequest: FiscalInvoiceRequest): Result<JsonObject> {
        return try {
            val response = api.fiscalizeInvoice(invoiceRequest)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(errorHandler.parseError(response))
            }
        } catch (e: Exception) {
            Result.failure(errorHandler.traceErrorException(e))
        }
    }
}