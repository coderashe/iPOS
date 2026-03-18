package com.example.learncompose303.data.repositories

import android.util.Log
import com.example.learncompose303.data.data_source.CashFlowDao
import com.example.learncompose303.data.data_source.DayReportsDao
import com.example.learncompose303.data.data_source.DiscountsReceivedDao
import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.data_source.StockTakeDao
import com.example.learncompose303.data.data_source.TradeReceivablesDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackfillRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val expenseDao: ExpenseDao,
    private val paymentDao: PaymentDao,
    private val cashFlowDao: CashFlowDao,
    private val stockTakeDao: StockTakeDao,
    private val dayReportsDao: DayReportsDao,
    private val tradeReceivablesDao: TradeReceivablesDao,
    private val discountsReceivedDao: DiscountsReceivedDao,
    private val orderDao: OrderDao
) {
    companion object {
        private const val TAG = "BackfillRepository"
    }
    /**
     * Updates all existing records that have an empty deviceId with the current
     * deviceId and storeId. Safe to call on every login — only touches rows
     * where deviceId is blank or null.
     */
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String) {
        if (deviceId.isBlank() || storeId.isBlank()) {
            Log.w(TAG, "⚠️ Skipping backfill — deviceId='$deviceId' storeId='$storeId' (one or both are blank)")
            return
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🚀 Starting backfill")
        Log.d(TAG, "   deviceId = $deviceId")
        Log.d(TAG, "   storeId  = $storeId")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        run("invoice")          { invoiceDao.backfillDeviceAndStore(deviceId, storeId) }
        run("expenses")         { expenseDao.backfillDeviceAndStore(deviceId, storeId) }
        run("payment")          { paymentDao.backfillDeviceAndStore(deviceId, storeId) }
        run("cashflow")         { cashFlowDao.backfillDeviceAndStore(deviceId, storeId) }
        run("stock_take")       { stockTakeDao.backfillDeviceAndStore(deviceId, storeId) }
        run("day_reports")      { dayReportsDao.backfillDeviceAndStore(deviceId, storeId) }
        run("tradereceivables") { tradeReceivablesDao.backfillDeviceAndStore(deviceId, storeId) }
        run("discountsreceived"){ discountsReceivedDao.backfillDeviceAndStore(deviceId, storeId) }
        run("orders")           { orderDao.backfillDeviceAndStore(deviceId, storeId) }

        Log.d(TAG, "✅ Backfill complete for all tables")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private suspend fun run(table: String, block: suspend () -> Unit) {
        try {
            block()
            Log.d(TAG, "   ✔ $table — updated")
        } catch (e: Exception) {
            Log.e(TAG, "   ✘ $table — FAILED: ${e.message}", e)
        }
    }
}
