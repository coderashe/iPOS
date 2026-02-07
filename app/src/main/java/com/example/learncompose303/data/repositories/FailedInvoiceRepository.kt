package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.FailedInvoiceDao
import com.example.learncompose303.data.entities.FailedInvoiceItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FailedInvoiceRepository @Inject constructor(
    private val failedInvoiceDao: FailedInvoiceDao
) {
    fun getAllFailedInvoices(): Flow<List<FailedInvoiceItem>> = 
        failedInvoiceDao.getAllFailedInvoices()
    
    suspend fun insertFailedInvoices(items: List<FailedInvoiceItem>) = 
        failedInvoiceDao.insertFailedInvoices(items)
    
    suspend fun deleteFailedInvoice(item: FailedInvoiceItem) = 
        failedInvoiceDao.deleteFailedInvoice(item)
    
    suspend fun clearAll() = 
        failedInvoiceDao.clearAll()
}
