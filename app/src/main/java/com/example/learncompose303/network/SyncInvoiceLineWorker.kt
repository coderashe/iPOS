package com.example.learncompose303.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncInvoiceLineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val invoiceLineRepository: InvoiceLineRepository,
    private val invoiceLineApiService: InvoiceLineApiService,
    private val connectivityHelper: ConnectivityHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "backIntCheck"
        const val KEY_INVOICE_LINE_ID = "invoice_line_id"
        const val KEY_BATCH_SYNC = "batch_sync"
        const val KEY_RETRY = "retry"
        private const val BATCH_SIZE = 200
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "SyncInvoiceLineWorker.doWork() started")
        
        return@withContext try {
            // Check network connectivity first
            if (!connectivityHelper.isNetworkAvailable()) {
                Log.w(TAG, "No network connection available, will retry later")
                return@withContext Result.retry()
            }

            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val invoiceLineId = inputData.getInt(KEY_INVOICE_LINE_ID, -1)
            
            Log.d(TAG, "Work parameters - isBatchSync: $isBatchSync, invoiceLineId: $invoiceLineId")

            if (isBatchSync) {
                Log.d(TAG, "Starting batch sync")
                syncBatchInvoiceLines()
            } else if (invoiceLineId != -1) {
                Log.d(TAG, "Starting single sync for ID: $invoiceLineId")
                syncSingleInvoiceLine(invoiceLineId)
            } else {
                Log.e(TAG, "No valid sync parameters provided")
                Result.failure()
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server", e)
            Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout: Server not responding", e)
            Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Network I/O error", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with exception: ${e.message}", e)
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
                val errorCode = response.code()
                Log.e(TAG, "Failed to sync invoice line: $errorCode - ${response.message()}")
                
                // Don't retry on client errors (4xx)
                if (errorCode in 400..499) {
                    Log.e(TAG, "Client error ($errorCode), not retrying")
                    return Result.failure()
                }
                
                Result.retry()
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Cannot reach server: ${e.message}", e)
            Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Server timeout: ${e.message}", e)
            Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Network I/O error: ${e.message}", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single invoice line", e)
            Result.retry()
        }
    }

    private suspend fun syncBatchInvoiceLines(): Result {
        Log.d(TAG, "syncBatchInvoiceLines() started")
        
        return try {
            val unsyncedInvoiceLines = invoiceLineRepository.getUnsyncedInvoiceLines()
            Log.d(TAG, "Retrieved ${unsyncedInvoiceLines.size} unsynced invoice lines from database")
            
            if (unsyncedInvoiceLines.isEmpty()) {
                Log.d(TAG, "No invoice lines to sync - returning success")
                return Result.success()
            }

            Log.d(TAG, "Starting individual sync for ${unsyncedInvoiceLines.size} invoice lines")
            Log.d(TAG, "First few invoice lines: ${unsyncedInvoiceLines.take(3).map { "ID:${it.id}, InvoiceID:${it.invoiceId}" }}")

            // Process individually since batch endpoint doesn't exist
            unsyncedInvoiceLines.forEachIndexed { index, invoiceLine ->
                Log.d(TAG, "Processing invoice line ${index + 1}/${unsyncedInvoiceLines.size} - ID: ${invoiceLine.id}")
                
                val dto = invoiceLine.toDto()
                Log.d(TAG, "Converted to DTO, making API call to save single invoice line")
                
                val response = invoiceLineApiService.saveInvoiceLine(dto)
                Log.d(TAG, "API response - Success: ${response.isSuccessful}, Code: ${response.code()}")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully synced invoice line ${index + 1} - ID: ${invoiceLine.id}")
                    // Mark as synced
                    invoiceLineRepository.markInvoiceLineAsSynced(invoiceLine.id)
                    response.body()?.let { responseBody ->
                        Log.d(TAG, "Response body ID: ${responseBody.id}")
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e(TAG, "Failed to sync invoice line ${index + 1}: $errorCode - $errorBody")
                    
                    // Don't retry on client errors (4xx) - skip this item and continue
                    if (errorCode in 400..499) {
                        Log.e(TAG, "Client error ($errorCode), skipping this item")
                        // Continue to next item instead of returning
                    } else {
                        // Server error - retry the whole batch
                        return Result.retry()
                    }
                }
            }

            Log.d(TAG, "Completed sync for all ${unsyncedInvoiceLines.size} invoice lines")
            
            // Show success toast on main thread
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    applicationContext,
                    "${unsyncedInvoiceLines.size} invoice lines synced successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
            Result.retry()
        }
    }
}