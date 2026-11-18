package com.ipos.network

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles check inside doWork()
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
     * Sync all data types immediately, including the full entity DAG.
     */
    fun syncAllNow() {
        scheduleProductBatchSync()
        scheduleInvoiceBatchSync()
        scheduleInvoiceLineBatchSync()
        scheduleFullSync()
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }

    /**
     * Schedule a full sync for all entities.
     * Enqueued as parallel requests under unique work "full_sync" so failures in optional tables
     * (e.g. expenses, day reports) never block core tables (products, invoices, devices).
     */
    fun scheduleFullSync() {
        // Cancel any existing sync chain before starting a new one
        workManager.cancelUniqueWork("full_sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // ConnectivityHelper handles the check inside doWork()
            .build()

        fun buildRequest(clazz: Class<out androidx.work.ListenableWorker>) =
            androidx.work.OneTimeWorkRequest.Builder(clazz)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, SYNC_BACKOFF_DELAY, TimeUnit.MINUTES)
                .setInputData(workDataOf(BaseSyncWorker.KEY_RETRY to false, "batchSync" to true))
                .build()

        val allRequests = listOf(
            buildRequest(SyncDeviceWorker::class.java),
            buildRequest(SyncUserWorker::class.java),
            buildRequest(SyncCustomerWorker::class.java),
            buildRequest(SyncProductWorker::class.java),
            buildRequest(SyncPriceListWorker::class.java),
            buildRequest(SyncInvoiceWorker::class.java),
            buildRequest(SyncInvoiceLineWorker::class.java),
            buildRequest(SyncPaymentWorker::class.java),
            buildRequest(SyncShoeBrandWorker::class.java),
            buildRequest(SyncShoeSizeWorker::class.java),
            buildRequest(SyncCartItemWorker::class.java),
            buildRequest(SyncStockTakeWorker::class.java),
            buildRequest(SyncProductDayReportWorker::class.java),
            buildRequest(SyncDayReportsWorker::class.java),
            buildRequest(SyncCashFlowWorker::class.java),
            buildRequest(SyncExpenseWorker::class.java),
            buildRequest(SyncTradeReceivablesWorker::class.java),
            buildRequest(SyncDiscountsReceivedWorker::class.java),
            buildRequest(SyncCurrencyWorker::class.java)
        )

        workManager
            .beginUniqueWork("full_sync", ExistingWorkPolicy.REPLACE, allRequests)
            .enqueue()
    }

    /**
     * Reset unsynced counters (call after successful batch sync)
     */
    fun resetCounters() {
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }
}

