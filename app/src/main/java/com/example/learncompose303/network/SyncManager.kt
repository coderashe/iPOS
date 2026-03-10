package com.example.learncompose303.network

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized sync manager for handling all data synchronization operations.
 * Automatically triggers sync workers when data is created or updated.
 */
@Singleton
class SyncManager @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_BACKOFF_DELAY = 15L
        private const val BATCH_SYNC_THRESHOLD = 10
    }

    // Track unsynced counts for batch sync optimization
    private var unsyncedInvoiceCount = 0
    private var unsyncedProductCount = 0

    /**
     * Schedule sync for a single invoice immediately after creation/update
     */
    fun scheduleSingleInvoiceSync(invoiceId: Long) {
        Log.d(TAG, "Scheduling immediate sync for invoice $invoiceId")

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoiceWorker>()
            .setInputData(
                workDataOf(
                    SyncInvoiceWorker.KEY_INVOICE_ID to invoiceId,
                    SyncInvoiceWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_invoice_$invoiceId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        // Check if batch sync is needed
        unsyncedInvoiceCount++
        if (unsyncedInvoiceCount >= BATCH_SYNC_THRESHOLD) {
            scheduleInvoiceBatchSync()
            unsyncedInvoiceCount = 0
        }
    }

    /**
     * Schedule batch sync for all unsynced invoices
     */
    fun scheduleInvoiceBatchSync() {
        Log.d(TAG, "Scheduling batch sync for invoices")

        val batchSyncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoiceWorker>()
            .setInputData(
                workDataOf(
                    SyncInvoiceWorker.KEY_BATCH_SYNC to true,
                    SyncInvoiceWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "invoice_batch_sync",
            ExistingWorkPolicy.REPLACE,
            batchSyncWorkRequest
        )
    }

    /**
     * Schedule sync for a single product immediately after creation/update
     */
    fun scheduleSingleProductSync(productId: Long) {
        Log.d(TAG, "Scheduling immediate sync for product $productId")

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncProductWorker>()
            .setInputData(
                workDataOf(
                    SyncProductWorker.KEY_PRODUCT_ID to productId,
                    SyncProductWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_product_$productId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        // Check if batch sync is needed
        unsyncedProductCount++
        if (unsyncedProductCount >= BATCH_SYNC_THRESHOLD) {
            scheduleProductBatchSync()
            unsyncedProductCount = 0
        }
    }

    /**
     * Schedule batch sync for all unsynced products
     */
    fun scheduleProductBatchSync() {
        Log.d(TAG, "Scheduling batch sync for products")

        val batchSyncWorkRequest = OneTimeWorkRequestBuilder<SyncProductWorker>()
            .setInputData(
                workDataOf(
                    SyncProductWorker.KEY_BATCH_SYNC to true,
                    SyncProductWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "product_batch_sync",
            ExistingWorkPolicy.REPLACE,
            batchSyncWorkRequest
        )
    }

    /**
     * Schedule sync for invoice lines
     */
    fun scheduleInvoiceLineSync(invoiceLineId: Int) {
        Log.d(TAG, "Scheduling immediate sync for invoice line $invoiceLineId")

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoiceLineWorker>()
            .setInputData(
                workDataOf(
                    SyncInvoiceLineWorker.KEY_INVOICE_LINE_ID to invoiceLineId,
                    SyncInvoiceLineWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_invoice_line_$invoiceLineId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    /**
     * Schedule batch sync for all unsynced invoice lines
     */
    fun scheduleInvoiceLineBatchSync() {
        Log.d(TAG, "Scheduling batch sync for invoice lines")

        val batchSyncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoiceLineWorker>()
            .setInputData(
                workDataOf(
                    SyncInvoiceLineWorker.KEY_BATCH_SYNC to true,
                    SyncInvoiceLineWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                SYNC_BACKOFF_DELAY,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "invoice_line_batch_sync",
            ExistingWorkPolicy.REPLACE,
            batchSyncWorkRequest
        )
    }

    /**
     * Sync all data types immediately
     */
    fun syncAllNow() {
        Log.d(TAG, "Triggering immediate sync for all data types")
        scheduleInvoiceBatchSync()
        scheduleProductBatchSync()
        scheduleInvoiceLineBatchSync()
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }

    /**
     * Reset unsynced counters (call after successful batch sync)
     */
    fun resetCounters() {
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }
}
