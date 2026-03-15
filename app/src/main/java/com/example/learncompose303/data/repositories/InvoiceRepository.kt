package com.example.learncompose303.data.repositories

import android.util.Log
import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.entities.Currency
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.MonthlySales
import com.example.learncompose303.network.InvoiceApiService
import com.example.learncompose303.network.InvoiceDto
import com.example.learncompose303.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface InvoiceRepository {
    suspend fun insertInvoice(invoice: Invoice): Long
    suspend fun getInvoiceById(id: Long): Invoice?
    suspend fun getAllInvoices(): Flow<List<Invoice>>
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteInvoice(invoice: Invoice)
    suspend fun getInvoicesByDate(startDate: Long): Flow<List<Invoice>>
    suspend fun getSalesPerMonth(): Flow<List<MonthlySales>>
    suspend fun getExchangeRate(currencyCode: String): Double
    suspend fun getAllCurrencies(): Flow<List<Currency>>
    suspend fun insertCurrencies(currencies: List<Currency>)
    suspend fun insertCurrency(currency: Currency)
    suspend fun getAllCurrenciesOnce(): List<Currency>
    suspend fun getTotalSalesPerCurrency(): Flow<Map<String, Double>>
    suspend fun saveInvoiceRemote(invoiceDto: InvoiceDto): NetworkResult<InvoiceDto>
    suspend fun getUnsyncedCount(): Flow<Int>

}

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val currencyDao: CurrencyDao,
    private val invoiceApiService: InvoiceApiService,
    private val syncManager: com.example.learncompose303.network.SyncManager
) : InvoiceRepository {

    private val TAG = "InvoiceRepositoryImpl"

    override suspend fun insertInvoice(invoice: Invoice): Long = withContext(Dispatchers.IO) {
        // Set current time as timestamp and mark for sync
        val timestampedInvoice = invoice.copy(
            lastUpdated = null // Null lastUpdated means it needs to be synced
        )

        // Insert into local database
        val id = invoiceDao.insertInvoice(timestampedInvoice)

        // Automatically trigger sync for the new invoice
        if (id > 0) {
            syncManager.scheduleSingleInvoiceSync(id)
            Log.d(TAG, "Auto-sync scheduled for new invoice: $id")
        }

        return@withContext id
    }

    override suspend fun updateInvoice(invoice: Invoice): Unit = withContext(Dispatchers.IO) {
        // Mark as needing sync by setting lastUpdated to null
        val updatedInvoice = invoice.copy(lastUpdated = null)
        invoiceDao.updateInvoice(updatedInvoice)

        // Automatically trigger sync for the updated invoice
        syncManager.scheduleSingleInvoiceSync(updatedInvoice.invoiceId)
        Log.d(TAG, "Auto-sync scheduled for updated invoice: ${updatedInvoice.invoiceId}")
    }

    override suspend fun deleteInvoice(invoice: Invoice): Unit = withContext(Dispatchers.IO) {
        // For now, just delete locally
        // A more complete implementation would track deleted invoices for sync
        invoiceDao.deleteInvoice(invoice)

        // If you want to sync deletions with server:
        try {
            invoiceApiService.deleteInvoice(invoice.invoiceId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete invoice on server: ${e.message}")
            // Consider tracking failed deletions for retry
        }
    }

    override suspend fun getUnsyncedCount(): Flow<Int> {
        return invoiceDao.getUnsyncedCount()
    }

    override suspend fun saveInvoiceRemote(invoiceDto: InvoiceDto): NetworkResult<InvoiceDto> {
        return try {
            Log.d(TAG, "Attempting to save invoice: ${invoiceDto.invoiceId}")
            val response = invoiceApiService.saveInvoice(invoiceDto)

            if (response.isSuccessful) {
                Log.d(TAG, "Successfully saved invoice to remote database")
                NetworkResult.Success(response.body()!!)
            } else {
                Log.e(TAG, "Failed to save invoice remotely: ${response.code()}")
                Log.e(TAG, "Error body: ${response.errorBody()?.string() ?: "No error details"}")
                NetworkResult.Error("Failed to save invoice: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error saving invoice remotely: ${e.message}")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            NetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getSalesPerMonth(): Flow<List<MonthlySales>> {
        return withContext(Dispatchers.IO) {
            val currenciesFlow = currencyDao.getAllCurrencies()

            invoiceDao.getSalesPerMonthWithCurrency()
                .combine(currenciesFlow) { sales, currencies ->
                    val currencyMap = currencies.associateBy { it.code }

                    // Group by month and sum converted amounts
                    sales.groupBy { it.monthNumber }
                        .map { (month, salesInMonth) ->
                            val convertedTotal = salesInMonth.sumOf { sale ->
                                if (sale.currencyCode == "USD") {
                                    sale.totalAmount
                                } else {
                                    val rate = currencyMap[sale.currencyCode]?.rateToBase ?: 1.0
                                    sale.totalAmount / rate // Convert to USD
                                }
                            }

                            MonthlySales(month, convertedTotal)
                        }
                        .sortedBy { it.monthNumber }
                }
        }
    }

    override suspend fun getTotalSalesPerCurrency(): Flow<Map<String, Double>> {
        return getAllInvoices().map { invoices ->
            invoices.groupBy { it.currencyCode }.mapValues { entry ->
                entry.value.sumOf { it.totalAmount }
            }
        }
    }

    override suspend fun insertCurrency(currency: Currency) {
        currencyDao.insertCurrency(currency)
    }

    override suspend fun getAllCurrenciesOnce(): List<Currency> {
        return withContext(Dispatchers.IO) {
            currencyDao.getAllCurrenciesOnce()
        }
    }

    override suspend fun insertCurrencies(currencies: List<Currency>) {
        currencyDao.insertCurrencies(currencies)
    }

    override suspend fun getAllCurrencies(): Flow<List<Currency>> {
        return withContext(Dispatchers.IO) {
            currencyDao.getAllCurrencies()
        }
    }

    override suspend fun getExchangeRate(currencyCode: String): Double {
        return currencyDao.getExchangeRate(currencyCode) ?: 1.0 // Default to USD (1.0)
    }

//    override suspend fun insertInvoice(invoice: Invoice): Long {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.insertInvoice(invoice)
//        }
//    }

//    override suspend fun updateInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.updateInvoice(invoice)
//        }
//    }


    override suspend fun getInvoicesByDate(startDate: Long): Flow<List<Invoice>> {
        return withContext(Dispatchers.IO) {
            invoiceDao.getInvoicesByDate(startDate)
        }
    }

//    override suspend fun deleteInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.deleteInvoice(invoice)
//        }
//    }

    override suspend fun getInvoiceById(id: Long): Invoice? {
        return withContext(Dispatchers.IO) {
            invoiceDao.getInvoiceById(id)
        }
    }

    override suspend fun getAllInvoices(): Flow<List<Invoice>> {
        return withContext(Dispatchers.IO) {
            invoiceDao.getAllInvoices()
        }
    }

}

data class MonthlySales(
    val monthNumber: String,
    val totalSales: Double,
    val monthName: String = getMonthName(monthNumber)
)

private fun getMonthName(monthNumber: String): String {
    return when (monthNumber) {
        "01" -> "January"
        "02" -> "February"
        "03" -> "March"
        "04" -> "April"
        "05" -> "May"
        "06" -> "June"
        "07" -> "July"
        "08" -> "August"
        "09" -> "September"
        "10" -> "October"
        "11" -> "November"
        "12" -> "December"
        else -> "Unknown"
    }
}