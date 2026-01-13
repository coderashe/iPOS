package com.example.learncompose303.network

import com.example.learncompose303.data.entities.InvoiceLine
import retrofit2.Response
import retrofit2.http.*

// DTO for API communication
data class InvoiceLineDto(
    val id: Int = 0,
    val invoiceId: Long,
    val invoiceStatus: String,
    val productID: Long,
    val productName: String,
    val productPrice: Double,
    val productQtySold: Int,
    val cashier: String,
    val customerID: Long,
    val customerName: String,
    val customerPhone: String?,
    val totalAmount: Double,
    val amountTendered: Double?,
    val changeBalance: Double?,
    val invoiceDate: Long,
    val taxAmount: Double? = null,
    val currentStock: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

// Invoice Line API service interface
interface InvoiceLineApiService {
    @POST("/api/invoicelines")
    suspend fun saveInvoiceLine(@Body invoiceLine: InvoiceLineDto): Response<InvoiceLineDto>

    @POST("/api/invoicelines/batch")
    suspend fun saveInvoiceLines(@Body invoiceLines: List<InvoiceLineDto>): Response<List<InvoiceLineDto>>

    @GET("/api/invoicelines/{id}")
    suspend fun getInvoiceLineById(@Path("id") id: Int): Response<InvoiceLineDto>

    @GET("/api/invoicelines")
    suspend fun getAllInvoiceLines(): Response<List<InvoiceLineDto>>

    @DELETE("/api/invoicelines/{id}")
    suspend fun deleteInvoiceLine(@Path("id") id: Int): Response<Void>

    @PUT("/api/invoicelines/{id}")
    suspend fun updateInvoiceLine(@Path("id") id: Int, @Body invoiceLine: InvoiceLineDto): Response<InvoiceLineDto>
}

// Extension functions to convert between domain model and DTO
fun InvoiceLine.toDto(): InvoiceLineDto {
    return InvoiceLineDto(
        id = this.id,
        invoiceId = this.invoiceId,
        invoiceStatus = this.invoiceStatus,
        productID = this.productID,
        productName = this.productName,
        productPrice = this.productPrice,
        productQtySold = this.productQtySold,
        cashier = this.cashier,
        customerID = this.customerID,
        customerName = this.customerName,
        customerPhone = this.customerPhone,
        totalAmount = this.totalAmount,
        amountTendered = this.amountTendered,
        changeBalance = this.changeBalance,
        invoiceDate = this.invoiceDate,
        taxAmount = this.taxAmount,
        currentStock = this.currentStock,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun InvoiceLineDto.toEntity(): InvoiceLine {
    return InvoiceLine(
        id = this.id,
        invoiceId = this.invoiceId,
        invoiceStatus = this.invoiceStatus,
        productID = this.productID,
        productName = this.productName,
        productPrice = this.productPrice,
        productQtySold = this.productQtySold,
        cashier = this.cashier,
        customerID = this.customerID,
        customerName = this.customerName,
        customerPhone = this.customerPhone,
        totalAmount = this.totalAmount,
        amountTendered = this.amountTendered,
        changeBalance = this.changeBalance,
        invoiceDate = this.invoiceDate,
        taxAmount = this.taxAmount,
        currentStock = this.currentStock,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}