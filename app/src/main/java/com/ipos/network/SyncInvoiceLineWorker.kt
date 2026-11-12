package com.ipos.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ipos.data.repositories.InvoiceLineRepository
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
        const val KEY_BATCH_SYNC = "batchSync"
        const val KEY_RETRY = "retry"
        private const val BATCH_SIZE = 200
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!connectivityHelper.isNetworkAvailable()) {
                Log.w(TAG, "SyncInvoiceLineWorker — no network, retrying")
                return@withContext Result.retry()
            }

            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val invoiceLineId = inputData.getInt(KEY_INVOICE_LINE_ID, -1)

            if (isBatchSync) {
                syncBatchInvoiceLines()
            } else if (invoiceLineId != -1) {
                syncSingleInvoiceLine(invoiceLineId)
            } else {
                Log.e(TAG, "SyncInvoiceLineWorker — no valid sync parameters")
                Result.failure()
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "SyncInvoiceLineWorker — cannot reach server", e)
            Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "SyncInvoiceLineWorker — server timeout", e)
            Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "SyncInvoiceLineWorker — IOException mid-batch, retrying: ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "SyncInvoiceLineWorker — unexpected error: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun syncSingleInvoiceLine(invoiceLineId: Int): Result {
        return try {
            val invoiceLine = invoiceLineRepository.getInvoiceLineByIdOnce(invoiceLineId)
            if (invoiceLine == null) {
                Log.e(TAG, "SyncInvoiceLineWorker — invoice line not found: $invoiceLineId")
                return Result.failure()
            }

            val response = invoiceLineApiService.saveInvoiceLine(invoiceLine.toDto())
            if (response.isSuccessful) {
                Result.success()
            } else {
                val errorCode = response.code()
                Log.e(TAG, "SyncInvoiceLineWorker — failed to sync line $invoiceLineId: $errorCode")
                if (errorCode in 400..499) return Result.failure()
                Result.retry()
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "SyncInvoiceLineWorker — cannot reach server: ${e.message}")
            Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "SyncInvoiceLineWorker — server timeout: ${e.message}")
            Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "SyncInvoiceLineWorker — IOException: ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "SyncInvoiceLineWorker — error syncing single line", e)
            Result.retry()
        }
    }

    private suspend fun syncBatchInvoiceLines(): Result {
        return try {
            val unsyncedInvoiceLines = invoiceLineRepository.getUnsyncedInvoiceLines()

            if (unsyncedInvoiceLines.isEmpty()) {
                return Result.success()
            }

            Log.d(TAG, "SyncInvoiceLineWorker — syncing ${unsyncedInvoiceLines.size} invoice lines")

            unsyncedInvoiceLines.forEachIndexed { index, invoiceLine ->
                val response = invoiceLineApiService.saveInvoiceLine(invoiceLine.toDto())

                if (response.isSuccessful) {
                    invoiceLineRepository.markInvoiceLineAsSynced(invoiceLine.id)
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e(TAG, "SyncInvoiceLineWorker — failed line ${invoiceLine.id}: $errorCode - $errorBody")
                    if (errorCode !in 400..499) return Result.retry()
                }
            }

            Log.d(TAG, "SyncInvoiceLineWorker — batch complete (${unsyncedInvoiceLines.size} lines)")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncInvoiceLineWorker — batch error: ${e.message}", e)
            Result.retry()
        }
    }
}