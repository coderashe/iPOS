package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.entities.HourPerformance
import com.example.learncompose303.data.entities.ProductPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface BusinessRepository {
    fun getTotalSalesInUsd(): Flow<Double>
    fun getTotalExpenses(): Flow<Double?>
    fun getTotalSales(): Flow<Double>
    fun getTopPerformingProducts(): Flow<List<ProductPerformance>>
    fun getPeakHours(): Flow<List<HourPerformance>>
    fun getSalesPerMonth(): Flow<Map<String, Double>>
}

class BusinessRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val currencyDao: CurrencyDao
) : BusinessRepository {

    override fun getTotalSalesInUsd(): Flow<Double> {
        return invoiceDao.getInvoicesWithCurrency().map { invoices ->
            val currencies = currencyDao.getAllCurrencies().first() // Get all currencies
            val currencyMap = currencies.associateBy { it.code }

            // Calculate total by converting each invoice to USD
            invoices.sumOf { invoice ->
                if (invoice.currencyCode == "USD") {
                    invoice.totalAmount
                } else {
                    val currency = currencyMap[invoice.currencyCode]
                    if (currency != null && currency.rateToBase > 0) {
                        // Convert to USD by dividing by the exchange rate
                        invoice.totalAmount / currency.rateToBase
                    } else {
                        0.0 // Skip if currency not found or invalid rate
                    }
                }
            }
        }
    }

    override fun getTotalSales(): Flow<Double> {
        return invoiceDao.getTotalSales()
    }

    override fun getTotalExpenses(): Flow<Double?> {
        return expenseDao.getTotalExpenses()
    }

    override fun getTopPerformingProducts(): Flow<List<ProductPerformance>> {
        return invoiceLineDao.getTopPerformingProducts()
    }

    override fun getPeakHours(): Flow<List<HourPerformance>> {
        return invoiceLineDao.getPeakSalesHours()
    }

    override fun getSalesPerMonth(): Flow<Map<String, Double>> {
        return invoiceDao.getSalesPerMonth()
            .map { list ->
                list.associate { it.monthName to it.totalSales } // Use monthName instead of monthNumber
            }
    }


}
