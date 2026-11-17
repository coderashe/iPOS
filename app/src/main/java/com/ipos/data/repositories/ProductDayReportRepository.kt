package com.ipos.data.repositories

import com.ipos.data.data_source.ProductDayReportDao
import com.ipos.data.entities.ProductDayReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ProductDayReportRepository {
    suspend fun getProductDayReportById(id: Long): ProductDayReport?
    fun getAllProductDayReports(): Flow<List<ProductDayReport>>
    suspend fun insertProductDayReport(productDayReport: ProductDayReport): Long
    suspend fun updateProductDayReport(productDayReport: ProductDayReport)
    suspend fun deleteProductDayReport(productDayReport: ProductDayReport)
}

class ProductDayReportRepositoryImpl @Inject constructor(
    private val productDayReportDao: ProductDayReportDao
) : ProductDayReportRepository {

    override suspend fun getProductDayReportById(id: Long): ProductDayReport? {
        return withContext(Dispatchers.IO) {
            productDayReportDao.getProductDayReportById(id)
        }
    }

    override fun getAllProductDayReports(): Flow<List<ProductDayReport>> {
        return productDayReportDao.getAllProductDayReports()
    }

    override suspend fun insertProductDayReport(productDayReport: ProductDayReport): Long {
        return withContext(Dispatchers.IO) {
            productDayReportDao.insertProductDayReport(productDayReport)
        }
    }

    override suspend fun updateProductDayReport(productDayReport: ProductDayReport) {
        return withContext(Dispatchers.IO) {
            productDayReportDao.updateProductDayReport(productDayReport)
        }
    }

    override suspend fun deleteProductDayReport(productDayReport: ProductDayReport) {
        return withContext(Dispatchers.IO) {
            productDayReportDao.deleteProductDayReport(productDayReport)
        }
    }
}
