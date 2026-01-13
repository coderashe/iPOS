package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.entities.InvoiceLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface InvoiceLineRepository {
    suspend fun insertInvoiceLine(invoiceLine: InvoiceLine)

    suspend fun getAllInvoiceLines(): Flow<List<InvoiceLine>>
    suspend fun getAllInvoiceLinesOnce(): List<InvoiceLine>

    // Get invoice lines by invoice ID
    suspend fun getInvoiceLineById(id: Long): InvoiceLine?
    suspend fun getInvoiceLineByIdOnce(id: Int): InvoiceLine?
    suspend fun getInvoiceLinesByInvoiceId(invoiceId: Long): List<InvoiceLine>
    suspend fun updateInvoiceLine(invoiceLine: InvoiceLine)
}

class InvoiceLineRepositoryImp @Inject constructor(
    private val invoiceLineDao: InvoiceLineDao
) : InvoiceLineRepository {

    override suspend fun updateInvoiceLine(invoiceLine: InvoiceLine) {
        withContext(Dispatchers.IO) {
            invoiceLineDao.updateInvoiceLine(invoiceLine)
        }
    }

    override suspend fun insertInvoiceLine(invoiceLine: InvoiceLine) {
        invoiceLineDao.insertInvoiceLine(invoiceLine) // Ensure you have this method in DAO
    }

    override suspend fun getAllInvoiceLines(): Flow<List<InvoiceLine>> {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getAllInvoiceLines()
        }
    }

    override suspend fun getInvoiceLineById(id: Long): InvoiceLine? {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getInvoiceLineById(id)
        }
    }

    override suspend fun getInvoiceLinesByInvoiceId(invoiceId: Long): List<InvoiceLine> {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getInvoiceLinesByInvoiceId(invoiceId)
        }
    }

    override suspend fun getAllInvoiceLinesOnce(): List<InvoiceLine> {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getAllInvoiceLinesOnce()
        }
    }

    override suspend fun getInvoiceLineByIdOnce(id: Int): InvoiceLine? {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getInvoiceLineByIdOnce(id)
        }
    }

}
