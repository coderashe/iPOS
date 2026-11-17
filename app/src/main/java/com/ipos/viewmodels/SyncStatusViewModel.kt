package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.data.data_source.*
import com.ipos.network.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SyncEntityStatus(val name: String, val unsynced: Int)

data class SyncStatusUiState(
    val entities: List<SyncEntityStatus> = emptyList(),
    val totalUnsynced: Int = 0
)

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val invoiceDao: InvoiceDao,
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val paymentDao: PaymentDao,
    private val cartItemDao: CartItemDao,
    private val shoeBrandDao: ShoeBrandDao,
    private val shoeSizeDao: ShoeSizeDao,
    private val currencyDao: CurrencyDao,
    private val expenseDao: ExpenseDao,
    private val priceListDao: PriceListDao,
    private val stockTakeDao: StockTakeDao,
    private val cashFlowDao: CashFlowDao,
    private val dayReportsDao: DayReportsDao,
    private val productDayReportDao: ProductDayReportDao,
    private val tradeReceivablesDao: TradeReceivablesDao,
    private val discountsReceivedDao: DiscountsReceivedDao,
    private val userDao: UserDao,
    private val deviceInfoDao: DeviceInfoDao
) : ViewModel() {

    private val names = listOf(
        "Invoice", "Product", "Customer", "Payment", "CartItem",
        "ShoeBrand", "ShoeSize", "Currency", "Expense", "PriceList",
        "StockTake", "CashFlow", "DayReports", "ProductDayReport",
        "TradeReceivables", "DiscountsReceived", "User", "Device"
    )

    val uiState: StateFlow<SyncStatusUiState> = combine(
        listOf(
            invoiceDao.getUnsyncedCount(),
            productDao.getUnsyncedCount(),
            customerDao.getUnsyncedCount(),
            paymentDao.getUnsyncedCount(),
            cartItemDao.getUnsyncedCount(),
            shoeBrandDao.getUnsyncedCount(),
            shoeSizeDao.getUnsyncedCount(),
            currencyDao.getUnsyncedCount(),
            expenseDao.getUnsyncedCount(),
            priceListDao.getUnsyncedCount(),
            stockTakeDao.getUnsyncedCount(),
            cashFlowDao.getUnsyncedCount(),
            dayReportsDao.getUnsyncedCount(),
            productDayReportDao.getUnsyncedCount(),
            tradeReceivablesDao.getUnsyncedCount(),
            discountsReceivedDao.getUnsyncedCount(),
            userDao.getUnsyncedCount(),
            deviceInfoDao.getUnsyncedCount()
        )
    ) { counts ->
        val intCounts = counts.map { it as Int }
        val entities = names.zip(intCounts).map { (name, count) ->
            SyncEntityStatus(name, count)
        }
        SyncStatusUiState(entities = entities, totalUnsynced = intCounts.sum())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatusUiState())

    fun syncNow() = syncManager.syncAllNow()
}
