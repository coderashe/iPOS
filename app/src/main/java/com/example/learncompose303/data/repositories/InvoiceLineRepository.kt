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
    suspend fun getUnsyncedInvoiceLines(): List<InvoiceLine>
    suspend fun markInvoiceLineAsSynced(id: Int)

    // Get invoice lines by invoice ID
    suspend fun getInvoiceLineById(id: Long): InvoiceLine?
    suspend fun getInvoiceLineByIdOnce(id: Int): InvoiceLine?
    suspend fun getInvoiceLinesByInvoiceId(invoiceId: Long): List<InvoiceLine>
    suspend fun updateInvoiceLine(invoiceLine: InvoiceLine)
}

class InvoiceLineRepositoryImp @Inject constructor(
    private val invoiceLineDao: InvoiceLineDao,
    private val syncManager: com.example.learncompose303.network.SyncManager
) : InvoiceLineRepository {

    companion object {
        private const val TAG = "InvoiceLineRepository"
    }

    override suspend fun updateInvoiceLine(invoiceLine: InvoiceLine) {
        withContext(Dispatchers.IO) {
            // Mark for sync
            val updatedLine = invoiceLine.copy(synced = false)
            invoiceLineDao.updateInvoiceLine(updatedLine)
            
            // Automatically trigger sync
            syncManager.scheduleInvoiceLineSync(invoiceLine.id)
            android.util.Log.d(TAG, "Auto-sync scheduled for updated invoice line: ${invoiceLine.id}")
        }
    }

    override suspend fun insertInvoiceLine(invoiceLine: InvoiceLine) {
        // Mark for sync (synced = false)
        val lineToInsert = invoiceLine.copy(synced = false)
        val insertedId = invoiceLineDao.insertInvoiceLine(lineToInsert)
        
        // Automatically trigger sync for the new invoice line
        if (insertedId > 0) {
            syncManager.scheduleInvoiceLineSync(insertedId.toInt())
            android.util.Log.d(TAG, "Auto-sync scheduled for new invoice line: $insertedId")
        }
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

    override suspend fun getUnsyncedInvoiceLines(): List<InvoiceLine> {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getUnsyncedInvoiceLines()
        }
    }

    override suspend fun markInvoiceLineAsSynced(id: Int) {
        withContext(Dispatchers.IO) {
            invoiceLineDao.markAsSynced(id)
        }
    }

    override suspend fun getInvoiceLineByIdOnce(id: Int): InvoiceLine? {
        return withContext(Dispatchers.IO) {
            invoiceLineDao.getInvoiceLineByIdOnce(id)
        }
    }

}
