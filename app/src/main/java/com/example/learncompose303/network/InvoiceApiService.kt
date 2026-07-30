package com.example.learncompose303.network

import com.example.learncompose303.data.entities.Invoice
import retrofit2.Response
import retrofit2.http.*

// Define the DTO for API communication
data class InvoiceDto(
    val invoiceId: Long = 0,
    val customerId: Long,
    val totalAmount: Double,
    val invoiceDate: Long,
    val dueDate: Long?,
    val theStatus: String,
    val currencyCode: String,
    val lastUpdated: Long? = null,
    val isArchived: Boolean = false,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val storeId: String? = null
)

// Invoice API service interface
interface InvoiceApiService {
    @POST("/api/invoices")
    suspend fun saveInvoice(@Body invoice: InvoiceDto): Response<InvoiceDto>

    @GET("/api/invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: Long): Response<InvoiceDto>

    @GET("/api/invoices")
    suspend fun getAllInvoices(): Response<List<InvoiceDto>>

    @DELETE("/api/invoices/{id}")
    suspend fun deleteInvoice(@Path("id") id: Long): Response<Void>

    @PUT("/api/invoices/{id}")
    suspend fun updateInvoice(@Path("id") id: Long, @Body invoice: InvoiceDto): Response<InvoiceDto>
}

// Extension function to convert between domain model and DTO
fun Invoice.toDto(deviceId: String? = null, deviceName: String? = null): InvoiceDto {
    return InvoiceDto(
        invoiceId = this.invoiceId,
        customerId = this.customerId,
        totalAmount = this.totalAmount,
        invoiceDate = this.invoiceDate,
        dueDate = this.dueDate,
        theStatus = when (this.status) {
            "Paid" -> "PAID"
            "Pending" -> "PENDING"
            "Overdue" -> "OVERDUE"
            else -> "UNKNOWN"
        },
        currencyCode = this.currencyCode,
        lastUpdated = this.lastUpdated,
        isArchived = this.isArchived,
        deviceId = deviceId,
        deviceName = deviceName,
        storeId = this.storeId.ifBlank { null }
    )
}

fun InvoiceDto.toEntity(): Invoice {
    return Invoice(
        invoiceId = this.invoiceId,
        customerId = this.customerId,
        totalAmount = this.totalAmount,
        invoiceDate = this.invoiceDate,
        dueDate = this.dueDate,
        status = this.theStatus,
        currencyCode = this.currencyCode,
        lastUpdated = this.lastUpdated,
        isArchived = this.isArchived
    )
}
