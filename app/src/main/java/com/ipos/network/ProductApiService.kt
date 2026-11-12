package com.ipos.network

import com.ipos.data.entities.Products
import retrofit2.Response
import retrofit2.http.*

// Product DTO for API communication
data class ProductDto(
    val productId: Long = 0,
    val productDescription: String,
    val productQty: Long,
    val brandId: Long,
    val shoeSizeId: Long,
    val saleable: Boolean,
    val purchasable: Boolean,
    val averageCost: Double,
    val standardCost: Double,
    val salesTaxCode: String?,
    val productCode: String,
    val inclusiveTaxed: Boolean,
    val imageUri: String?,
    val sellingPrice: Double = 0.0,  // Selling price from PriceList
    val unitCost: Double = 0.0,  // Unit cost from PriceList
    val markupPercentage: Double = 0.0,  // Markup percentage from PriceList
    val lastUpdated: Long? = null,
    val deviceId: String? = null,  // Device identifier for multi-device tracking
    val deviceName: String? = null  // User-friendly device name
)

interface ProductApiService {
    @POST("/api/products")
    suspend fun saveProduct(@Body product: ProductDto): Response<ProductDto>

    @POST("/api/products/batch")
    suspend fun saveProducts(@Body products: List<ProductDto>): Response<BatchSyncResult>

    @GET("/api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductDto>

    @GET("/api/products")
    suspend fun getAllProducts(): Response<List<ProductDto>>

    @DELETE("/api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Void>

    @PUT("/api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductDto): Response<ProductDto>
}

data class BatchSyncResult(val synced: Int)

// Extension functions for conversion between domain model and DTO
fun Products.toDto(
    deviceId: String? = null, 
    deviceName: String? = null,
    sellingPrice: Double = 0.0,
    unitCost: Double = 0.0,
    markupPercentage: Double = 0.0
): ProductDto {
    return ProductDto(
        productId = this.productId,
        productDescription = this.productDescription,
        productQty = this.productQty,
        brandId = this.brandID,
        shoeSizeId = this.shoeSizeId,
        saleable = this.saleable,
        purchasable = this.purchasable,
        averageCost = this.averageCost,
        standardCost = this.standardCost,
        salesTaxCode = this.salesTaxCode,
        productCode = this.productCode,
        inclusiveTaxed = this.inclusiveTaxed,
        imageUri = this.imageUri,
        sellingPrice = sellingPrice,
        unitCost = unitCost,
        markupPercentage = markupPercentage,
        lastUpdated = this.lastUpdated,
        deviceId = deviceId,
        deviceName = deviceName
    )
}

fun ProductDto.toEntity(): Products {
    return Products(
        productId = this.productId,
        productDescription = this.productDescription,
        productQty = this.productQty,
        brandID = this.brandId,
        shoeSizeId = this.shoeSizeId,
        saleable = this.saleable,
        purchasable = this.purchasable,
        averageCost = this.averageCost,
        standardCost = this.standardCost,
        salesTaxCode = this.salesTaxCode,
        productCode = this.productCode,
        inclusiveTaxed = this.inclusiveTaxed,
        imageUri = this.imageUri,
        lastUpdated = this.lastUpdated
    )
}