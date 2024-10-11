package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.entities.HourPerformance
import com.example.learncompose303.data.entities.ProductPerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface BusinessRepository {
    fun getTotalExpenses(): Flow<Double?>
    fun getTotalSales(): Flow<Double>
    fun getTopPerformingProducts(): Flow<List<ProductPerformance>>
    fun getPeakHours(): Flow<List<HourPerformance>>
    fun getSalesPerMonth(): Flow<Map<String, Double>>
}

class BusinessRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao
) : BusinessRepository {

    override fun getTotalExpenses(): Flow<Double?> {
        return expenseDao.getTotalExpenses()
    }

    override fun getTotalSales(): Flow<Double> {
        return invoiceDao.getTotalSales()
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
                list.associate { it.month to it.totalSales }
            }
    }

}
