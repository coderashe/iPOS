package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.entities.InvoiceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface InvoiceItemRepository {
    suspend fun getInvoiceItemsByInvoiceId(invoiceId: Long): Flow<List<InvoiceItem>> // Return a Flow
    suspend fun insertInvoiceItem(item: InvoiceItem): Long
    suspend fun deleteInvoiceItem(item: InvoiceItem)
    suspend fun updateInvoiceItem(invoiceItem: InvoiceItem)
}

class InvoiceItemRepositoryImpl @Inject constructor(
    private val invoiceItemDao: InvoiceItemDao
) : InvoiceItemRepository {

    override suspend fun updateInvoiceItem(invoiceItem: InvoiceItem) {
        withContext(Dispatchers.IO) {
            invoiceItemDao.updateInvoiceItem(invoiceItem)
        }
    }

    override suspend fun getInvoiceItemsByInvoiceId(invoiceId: Long): Flow<List<InvoiceItem>> {
        return invoiceItemDao.getItemsForInvoice(invoiceId) // Flow is returned directly
    }

    override suspend fun insertInvoiceItem(item: InvoiceItem): Long {
        return withContext(Dispatchers.IO) {
            invoiceItemDao.insertInvoiceItem(item)
        }
    }

    override suspend fun deleteInvoiceItem(item: InvoiceItem) {
        return withContext(Dispatchers.IO) {
            invoiceItemDao.deleteInvoiceItem(item)
        }
    }

}
