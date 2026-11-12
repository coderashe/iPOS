package com.ipos.network

import com.ipos.data.entities.FiscalInvoiceRequest
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FiscalizationApi {
    @POST("api/fiscal/cycle")
    suspend fun fiscalizeInvoice(
        @Body request: FiscalInvoiceRequest
    ): Response<JsonObject> // or create a proper response data class if you know the structure
}