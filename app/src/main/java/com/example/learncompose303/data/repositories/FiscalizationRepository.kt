package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.entities.FiscalInvoiceRequest
import com.example.learncompose303.network.ErrorHandler
import com.example.learncompose303.network.FiscalizationApi
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