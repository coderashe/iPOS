package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.entities.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface InvoiceRepository {
    suspend fun insertInvoice(invoice: Invoice): Long
    suspend fun getInvoiceById(id: Long): Invoice?
    suspend fun getAllInvoices(): Flow<List<Invoice>>
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteInvoice(invoice: Invoice)
}

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {

    override suspend fun insertInvoice(invoice: Invoice): Long {
        return withContext(Dispatchers.IO) {
            invoiceDao.insertInvoice(invoice)
        }
    }

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

    override suspend fun updateInvoice(invoice: Invoice) {
        return withContext(Dispatchers.IO) {
            invoiceDao.updateInvoice(invoice)
        }
    }

    override suspend fun deleteInvoice(invoice: Invoice) {
        return withContext(Dispatchers.IO) {
            invoiceDao.deleteInvoice(invoice)
        }
    }

}
