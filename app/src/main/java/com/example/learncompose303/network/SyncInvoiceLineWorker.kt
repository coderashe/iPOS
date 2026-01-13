package com.example.learncompose303.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncInvoiceLineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val invoiceLineRepository: InvoiceLineRepository,
    private val invoiceLineApiService: InvoiceLineApiService
) : CoroutineWorker(context, workerParams) {

    @AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): SyncInvoiceLineWorker
    }

    companion object {
        private const val TAG = "SyncInvoiceLineWorker"
        const val KEY_INVOICE_LINE_ID = "invoice_line_id"
        const val KEY_BATCH_SYNC = "batch_sync"
        const val KEY_RETRY = "retry"
        private const val BATCH_SIZE = 50
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val invoiceLineId = inputData.getInt(KEY_INVOICE_LINE_ID, -1)

            if (isBatchSync) {
                syncBatchInvoiceLines()
            } else if (invoiceLineId != -1) {
                syncSingleInvoiceLine(invoiceLineId)
            } else {
                Log.e(TAG, "No valid sync parameters provided")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.retry()
        }
    }

    private suspend fun syncSingleInvoiceLine(invoiceLineId: Int): Result {
        return try {
            val invoiceLine = invoiceLineRepository.getInvoiceLineByIdOnce(invoiceLineId)
            if (invoiceLine == null) {
                Log.e(TAG, "Invoice line not found with ID: $invoiceLineId")
                return Result.failure()
            }

            val response = invoiceLineApiService.saveInvoiceLine(invoiceLine.toDto())
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully synced invoice line: $invoiceLineId")
                Result.success()
            } else {
                Log.e(TAG, "Failed to sync invoice line: ${response.code()} - ${response.message()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single invoice line", e)
            Result.retry()
        }
    }

    private suspend fun syncBatchInvoiceLines(): Result {
        return try {
            val unsyncedInvoiceLines = invoiceLineRepository.getAllInvoiceLinesOnce()
            
            if (unsyncedInvoiceLines.isEmpty()) {
                Log.d(TAG, "No invoice lines to sync")
                return Result.success()
            }

            Log.d(TAG, "Starting batch sync for ${unsyncedInvoiceLines.size} invoice lines")

            // Process in batches
            unsyncedInvoiceLines.chunked(BATCH_SIZE).forEach { batch ->
                val dtoList = batch.map { it.toDto() }
                
                val response = invoiceLineApiService.saveInvoiceLines(dtoList)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully synced batch of ${batch.size} invoice lines")
                } else {
                    Log.e(TAG, "Failed to sync batch: ${response.code()} - ${response.message()}")
                    return Result.retry()
                }
            }

            Log.d(TAG, "Completed batch sync for all invoice lines")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during batch sync", e)
            Result.retry()
        }
    }
}