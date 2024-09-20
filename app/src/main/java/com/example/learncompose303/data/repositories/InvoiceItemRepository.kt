package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.entities.InvoiceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface InvoiceItemRepository {
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItem>
    suspend fun insertInvoiceItem(item: InvoiceItem): Long
    suspend fun deleteInvoiceItem(item: InvoiceItem)
}

class InvoiceItemRepositoryImpl @Inject constructor(
    private val invoiceItemDao: InvoiceItemDao
) : InvoiceItemRepository {
    override suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItem> {
        return withContext(Dispatchers.IO) {
            invoiceItemDao.getItemsForInvoice(invoiceId)
        }
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
