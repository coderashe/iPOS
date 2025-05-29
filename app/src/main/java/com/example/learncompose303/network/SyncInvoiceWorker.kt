package com.example.learncompose303.network

import android.annotation.SuppressLint
import com.example.learncompose303.data.data_source.InvoiceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@SuppressLint("RestrictedApi")
@HiltWorker
class SyncInvoiceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val invoiceDao: InvoiceDao,
    private val invoiceApiService: InvoiceApiService
) : CoroutineWorker(appContext, params) {

    // Required companion interface
    interface Factory : WorkerAssistedFactory<SyncInvoiceWorker>

    companion object {
        private const val TAG = "SyncInvoiceWorker"
        const val KEY_INVOICE_ID = "invoiceId"
        const val KEY_RETRY = "retry"
        const val KEY_BATCH_SYNC = "batchSync"
    }

    init {
        Log.d("SyncInvoiceWorker", "Worker injected successfully")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if this is a batch sync or single invoice sync
            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val shouldRetry = inputData.getBoolean(KEY_RETRY, true)

            if (isBatchSync) {
                return@withContext performBatchSync(shouldRetry)
            } else {
                val invoiceId = inputData.getLong(KEY_INVOICE_ID, -1L)
                if (invoiceId == -1L) {
                    Log.e(TAG, "Invalid invoice ID provided for sync")
                    return@withContext Result.failure()
                }
                return@withContext syncSingleInvoice(invoiceId, shouldRetry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sync worker", e)
            return@withContext if (inputData.getBoolean(KEY_RETRY, true)) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncSingleInvoice(invoiceId: Long, shouldRetry: Boolean): Result {
        // Get invoice from local database
        val invoice = invoiceDao.getInvoiceById(invoiceId) ?: run {
            Log.e(TAG, "Invoice with ID $invoiceId not found in local database")
            return Result.failure()
        }

        // Validate required fields
        if (invoice.status.isEmpty()) {
            Log.e(TAG, "Invoice $invoiceId has null/empty status, skipping sync")
            return Result.failure()
        }

        // Convert to DTO for API
        val invoiceDto = invoice.toDto()
        Log.e(TAG, "Trying to sync invoice $invoiceDto")

        return try {
            // Attempt to sync with remote server
            val response = invoiceApiService.saveInvoice(invoiceDto)
            if (response.isSuccessful) {
                // Update local invoice with server response data
                response.body()?.let { serverDto ->
                    // Update the local invoice with any server-provided data
                    val updatedInvoice = invoice.copy(
                        lastUpdated = System.currentTimeMillis()
                    )
                    invoiceDao.updateInvoice(updatedInvoice)
                    Log.d(TAG, "Successfully synced invoice $invoiceId")
                }
                Result.success()
            } else {
                Log.e(TAG, "Failed to sync invoice: ${response.code()}")
                Log.e(TAG, "Error body: ${response.errorBody()?.string() ?: "No error details"}")

                if (shouldRetry) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing invoice: ${e.message}", e)
            if (shouldRetry) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        try {
            val unsyncedInvoices = invoiceDao.getUnsyncedInvoices(50)
            if (unsyncedInvoices.isEmpty()) {
                Log.d(TAG, "No invoices to sync in batch")
                return Result.success()
            }

            Log.d(TAG, "Starting batch sync of ${unsyncedInvoices.size} invoices")
            var successCount = 0
            var skippedCount = 0

            for (invoice in unsyncedInvoices) {
                // Skip invoices with invalid status
                if (invoice.status.isEmpty()) {
                    Log.w(TAG, "Skipping invoice ${invoice.invoiceId} with invalid status")
                    skippedCount++
                    continue
                }

                Log.d(TAG, "Syncing invoice: ${invoice.invoiceId}")
                val result = syncSingleInvoice(invoice.invoiceId, false)
                if (result is Result.Success) {
                    successCount++
                }
            }

            Log.d(
                TAG,
                "Batch sync completed. Success: $successCount, Failed: ${unsyncedInvoices.size - successCount - skippedCount}, Skipped: $skippedCount"
            )

            return when {
                successCount == unsyncedInvoices.size -> Result.success()
                successCount > 0 && !shouldRetry -> Result.success() // Partial success
                shouldRetry -> Result.retry()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch sync", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }

}
