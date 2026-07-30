package com.example.learncompose303.network

import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.CashFlow
import com.example.learncompose303.data.entities.Currency
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.DayReports
import com.example.learncompose303.data.entities.DiscountsReceived
import com.example.learncompose303.data.entities.Expense
import com.example.learncompose303.data.entities.Payment
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.ProductDayReport
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.data.entities.TradeReceivables
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ─── Response ────────────────────────────────────────────────────────────────

data class SyncResult(val synced: Int, val errors: List<String> = emptyList())

// ─── DTOs ─────────────────────────────────────────────────────────────────────

data class CustomerSyncDto(
    val customerId: Long,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val customerType: String,
    val taxIdentificationNumber: String?,
    val loyaltyPoints: Int
)

data class ExpenseSyncDto(
    val expenseId: Long,
    val description: String,
    val amount: Double,
    val expenseDate: Long,
    val category: String,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class ShoeBrandSyncDto(
    val brandId: Long,
    val brandName: String
)

data class ShoeSizeSyncDto(
    val shoeSizeId: Long,
    val brandId: Long,
    val size: Double,
    val quantity: Long
)

data class CurrencySyncDto(
    val code: String,
    val name: String,
    val rateToBase: Double
)

data class PaymentSyncDto(
    val paymentId: Long,
    val invoiceId: Long,
    val paymentDate: Long,
    val amount: Double,
    val method: String,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class CartItemSyncDto(
    val cartItemId: Long,
    val customerId: Long,
    val productId: Long,
    val invoiceId: Long,
    val totalAmount: Double,
    val totalAmountTendered: Double,
    val change: Double,
    val taxAmount: Double?,
    val discountAmount: Double?,
    val createdAt: Long,
    val updatedAt: Long?,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class StockTakeSyncDto(
    val id: Int,
    val productId: Long,
    val recordedStock: Long,
    val countedStock: Double,
    val variance: Double,
    val varianceReason: String?,
    val dateRecorded: Long,
    val recordedBy: String,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class TradeReceivablesSyncDto(
    val receivableId: Long,
    val orderId: Long,
    val totalAmount: Double,
    val amountPaid: Double,
    val creditPeriod: Int,
    val dueDate: Long,
    val paymentStatus: String,
    val dateCreated: Long,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class DayReportsSyncDto(
    val reportId: Long,
    val totalInvoices: Int,
    val totalOrders: Int,
    val totalProductsSold: Int,
    val totalInvoiceSales: Double,
    val reportDate: Long,
    val startTime: Long,
    val endTime: Long,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class ProductDayReportSyncDto(
    val reportId: Long,
    val productId: Long,
    val productName: String,
    val openingStock: Double,
    val closingStock: Double,
    val quantitySold: Int,
    val totalSales: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val reportDate: Long
)

data class CashFlowSyncDto(
    val cashFlowId: Long,
    val orderId: Long,
    val amount: Double,
    val cashOutflow: Boolean,
    val transactionDate: Long,
    val description: String?,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class DiscountsReceivedSyncDto(
    val discountId: Long,
    val orderId: Long,
    val discountAmount: Double,
    val dateReceived: Long,
    val deviceId: String? = null,
    val storeId: String? = null
)

data class PriceListSyncDto(
    val priceListId: Long,
    val productId: Long,
    val productDescription: String,
    val unitCost: Double,
    val sellingPrice: Double,
    val markupPercentage: Double,
    val grossProfitPercentage: Double
)

// ─── toSyncDto() extensions ───────────────────────────────────────────────────

fun Customer.toSyncDto() = CustomerSyncDto(
    customerId = customerId,
    name = name,
    email = email,
    phone = phone,
    address = address,
    customerType = customerType,
    taxIdentificationNumber = taxIdentificationNumber,
    loyaltyPoints = loyaltyPoints
)

fun Expense.toSyncDto() = ExpenseSyncDto(
    expenseId = expenseId,
    description = description,
    amount = amount,
    expenseDate = expenseDate,
    category = category,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun ShoeBrand.toSyncDto() = ShoeBrandSyncDto(
    brandId = brandID,
    brandName = brandName
)

fun ShoeSize.toSyncDto() = ShoeSizeSyncDto(
    shoeSizeId = shoeSizeId,
    brandId = brandID,
    size = size,
    quantity = quantity
)

fun Currency.toSyncDto() = CurrencySyncDto(
    code = code,
    name = name,
    rateToBase = rateToBase
)

fun Payment.toSyncDto() = PaymentSyncDto(
    paymentId = paymentId,
    invoiceId = invoiceId,
    paymentDate = paymentDate,
    amount = amount,
    method = method,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun CartItem.toSyncDto() = CartItemSyncDto(
    cartItemId = cartItemId,
    customerId = customerId,
    productId = productId,
    invoiceId = invoiceId,
    totalAmount = totalAmount,
    totalAmountTendered = totalAmountTendered,
    change = change,
    taxAmount = taxAmount,
    discountAmount = discountAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deviceId = null,   // CartItem entity has no deviceId/storeId — safe default
    storeId = null
)

fun StockTake.toSyncDto() = StockTakeSyncDto(
    id = id,
    productId = productId,
    recordedStock = recordedStock,
    countedStock = countedStock,
    variance = variance,
    varianceReason = varianceReason,
    dateRecorded = dateRecorded,
    recordedBy = recordedBy,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun TradeReceivables.toSyncDto() = TradeReceivablesSyncDto(
    receivableId = receivableId,
    orderId = orderId,
    totalAmount = totalAmount,
    amountPaid = amountPaid,
    creditPeriod = creditPeriod,
    dueDate = dueDate,
    paymentStatus = paymentStatus,
    dateCreated = dateCreated,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun DayReports.toSyncDto() = DayReportsSyncDto(
    reportId = reportId,
    totalInvoices = totalInvoices,
    totalOrders = totalOrders,
    totalProductsSold = totalProductsSold,
    totalInvoiceSales = totalInvoiceSales,
    reportDate = reportDate,
    startTime = startTime,
    endTime = endTime,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun ProductDayReport.toSyncDto() = ProductDayReportSyncDto(
    reportId = reportId,
    productId = productId,
    productName = productName,
    openingStock = openingStock,
    closingStock = closingStock,
    quantitySold = quantitySold,
    totalSales = totalSales,
    totalCost = totalCost,
    totalProfit = totalProfit,
    reportDate = reportDate
)

fun CashFlow.toSyncDto() = CashFlowSyncDto(
    cashFlowId = cashFlowId,
    orderId = orderId,
    amount = amount,
    cashOutflow = cashOutflow,
    transactionDate = transactionDate,
    description = description,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun DiscountsReceived.toSyncDto() = DiscountsReceivedSyncDto(
    discountId = discountId,
    orderId = orderId,
    discountAmount = discountAmount,
    dateReceived = dateReceived,
    deviceId = deviceId.ifBlank { null },
    storeId = storeId.ifBlank { null }
)

fun PriceList.toSyncDto() = PriceListSyncDto(
    priceListId = priceListId,
    productId = productId,
    productDescription = productDescription,
    unitCost = unitCost,
    sellingPrice = sellingPrice,
    markupPercentage = markupPercentage,
    grossProfitPercentage = grossProfitPercentage
)

data class DeviceSyncDto(
    val deviceId: String,
    val deviceName: String,
    val storeId: String,
    val lastSeen: Long
)

fun com.example.learncompose303.data.entities.DeviceInfo.toSyncDto() = DeviceSyncDto(
    deviceId = deviceId,
    deviceName = deviceName,
    storeId = storeId,
    lastSeen = lastSeen
)

interface SyncApiService {

    @POST("/api/customers/sync")
    suspend fun syncCustomers(@Body dtos: List<CustomerSyncDto>): Response<SyncResult>

    @POST("/api/expenses/sync")
    suspend fun syncExpenses(@Body dtos: List<ExpenseSyncDto>): Response<SyncResult>

    @POST("/api/shoebrands/sync")
    suspend fun syncShoeBrands(@Body dtos: List<ShoeBrandSyncDto>): Response<SyncResult>

    @POST("/api/shoesizes/sync")
    suspend fun syncShoeSizes(@Body dtos: List<ShoeSizeSyncDto>): Response<SyncResult>

    @POST("/api/currencies/sync")
    suspend fun syncCurrencies(@Body dtos: List<CurrencySyncDto>): Response<SyncResult>

    @POST("/api/payments/sync")
    suspend fun syncPayments(@Body dtos: List<PaymentSyncDto>): Response<SyncResult>

    @POST("/api/cartitems/sync")
    suspend fun syncCartItems(@Body dtos: List<CartItemSyncDto>): Response<SyncResult>

    @POST("/api/stocktakes/sync")
    suspend fun syncStockTakes(@Body dtos: List<StockTakeSyncDto>): Response<SyncResult>

    @POST("/api/tradereceivables/sync")
    suspend fun syncTradeReceivables(@Body dtos: List<TradeReceivablesSyncDto>): Response<SyncResult>

    @POST("/api/dayreports/sync")
    suspend fun syncDayReports(@Body dtos: List<DayReportsSyncDto>): Response<SyncResult>

    @POST("/api/productdayreports/sync")
    suspend fun syncProductDayReports(@Body dtos: List<ProductDayReportSyncDto>): Response<SyncResult>

    @POST("/api/cashflows/sync")
    suspend fun syncCashFlows(@Body dtos: List<CashFlowSyncDto>): Response<SyncResult>

    @POST("/api/discountsreceived/sync")
    suspend fun syncDiscountsReceived(@Body dtos: List<DiscountsReceivedSyncDto>): Response<SyncResult>

    @POST("/api/pricelists/sync")
    suspend fun syncPriceLists(@Body dtos: List<PriceListSyncDto>): Response<SyncResult>

    @POST("/api/devices/sync")
    suspend fun syncDevices(@Body dtos: List<DeviceSyncDto>): Response<SyncResult>
}
