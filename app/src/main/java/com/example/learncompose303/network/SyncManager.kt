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
     * Sync all data types immediately, including the full entity DAG.
     */
    fun syncAllNow() {
        android.util.Log.d("backIntCheck", "SyncManager.syncAllNow() called — scheduling full DAG")
        scheduleFullSync()
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }

    /**
     * Schedule a full DAG sync for all entities, respecting FK dependency order.
     * Uses a timestamped unique name to avoid WorkManager chain conflicts.
     *
     * Layer 1 (no deps):  ShoeBrand, Currency, Expense, DayReports, CashFlow, DiscountsReceived, TradeReceivables, User, Device
     * Layer 2 (after L1): ShoeSize, Customer
     * Layer 3 (after L2): Product, PriceList
     * Layer 4 (after L3): Invoice
     * Layer 5 (after L4): InvoiceLine, Payment, CartItem, StockTake, ProductDayReport
     */
    fun scheduleFullSync() {

        // Cancel any existing sync chain before starting a new one
        workManager.cancelUniqueWork("full_sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun buildRequest(clazz: Class<out androidx.work.ListenableWorker>) =
            androidx.work.OneTimeWorkRequest.Builder(clazz)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, SYNC_BACKOFF_DELAY, TimeUnit.MINUTES)
                .setInputData(workDataOf(BaseSyncWorker.KEY_RETRY to true, "batchSync" to true))
                .build()

        val layer1 = listOf(
            buildRequest(SyncShoeBrandWorker::class.java),
            buildRequest(SyncCurrencyWorker::class.java),
            buildRequest(SyncExpenseWorker::class.java),
            buildRequest(SyncDayReportsWorker::class.java),
            buildRequest(SyncCashFlowWorker::class.java),
            buildRequest(SyncDiscountsReceivedWorker::class.java),
            buildRequest(SyncTradeReceivablesWorker::class.java),
            buildRequest(SyncUserWorker::class.java),
            buildRequest(SyncDeviceWorker::class.java)
        )

        val layer2 = listOf(
            buildRequest(SyncShoeSizeWorker::class.java),
            buildRequest(SyncCustomerWorker::class.java)
        )

        val layer3 = listOf(
            buildRequest(SyncProductWorker::class.java),
            buildRequest(SyncPriceListWorker::class.java)
        )

        val layer4 = listOf(
            buildRequest(SyncInvoiceWorker::class.java)
        )

        val layer5 = listOf(
            buildRequest(SyncInvoiceLineWorker::class.java),
            buildRequest(SyncPaymentWorker::class.java),
            buildRequest(SyncCartItemWorker::class.java),
            buildRequest(SyncStockTakeWorker::class.java),
            buildRequest(SyncProductDayReportWorker::class.java)
        )

        workManager
            .beginUniqueWork("full_sync", ExistingWorkPolicy.REPLACE, layer1)
            .then(layer2)
            .then(layer3)
            .then(layer4)
            .then(layer5)
            .enqueue()

        android.util.Log.d("backIntCheck", "SyncManager.scheduleFullSync() — DAG enqueued")
    }

    /**
     * Reset unsynced counters (call after successful batch sync)
     */
    fun resetCounters() {
        unsyncedInvoiceCount = 0
        unsyncedProductCount = 0
    }
}

