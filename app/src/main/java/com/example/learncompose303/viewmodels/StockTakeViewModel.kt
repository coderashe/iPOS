package com.example.learncompose303.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.data.domain.ProductWithBrand
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.data.entities.StockTakeSession
import com.example.learncompose303.data.repositories.PriceListRepository
import com.example.learncompose303.data.repositories.ProductRepository
import com.example.learncompose303.data.repositories.StockTakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI-facing state for the stock-take screen. */
data class StockTakeUiState(
    val isLoading: Boolean = true,
    val openSession: StockTakeSession? = null,
    val productsWithBrand: List<ProductWithBrand> = emptyList(),
    val filteredProducts: List<ProductWithBrand> = emptyList(),
    val countedProductIds: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
    /** Set when a "new product" dialog should be shown. */
    val showAddProductDialog: Boolean = false,
    /** Transient one-shot message for snackbars. */
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockTakeViewModel @Inject constructor(
    private val stockTakeRepository: StockTakeRepository,
    private val productRepository: ProductRepository,
    private val priceListRepository: PriceListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockTakeUiState())
    val uiState: StateFlow<StockTakeUiState> = _uiState.asStateFlow()

    val allSessions = stockTakeRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query lives separately so it doesn't need a full state rebuild
    private val _searchQuery = MutableStateFlow("")

    init {
        // Observe open session continuously
        viewModelScope.launch {
            stockTakeRepository.getOpenSession().collect { session ->
                _uiState.value = _uiState.value.copy(
                    openSession = session,
                    isLoading = false
                )
                // Re-load counted IDs when session changes
                if (session != null) observeCountedIds(session.sessionId)
            }
        }

        // Observe all products with brand and search query together
        viewModelScope.launch {
            combine(
                productRepository.getProductsWithBrand(),
                _searchQuery
            ) { productsWithBrand, query ->
                val filtered = if (query.isBlank()) productsWithBrand
                else productsWithBrand.filter {
                    it.product.productDescription.contains(query, ignoreCase = true) ||
                            it.product.productCode.contains(query, ignoreCase = true) ||
                            it.brand.brandName.contains(query, ignoreCase = true)
                }
                Pair(productsWithBrand, filtered)
            }.collect { (all, filtered) ->
                _uiState.value = _uiState.value.copy(
                    productsWithBrand = all,
                    filteredProducts = filtered,
                    totalCount = all.size,
                    searchQuery = _searchQuery.value
                )
            }
        }
    }

    private fun observeCountedIds(sessionId: Long) {
        viewModelScope.launch {
            stockTakeRepository.getCountedProductIds(sessionId).collect { ids ->
                _uiState.value = _uiState.value.copy(
                    countedProductIds = ids,
                    checkedCount = ids.size
                )
            }
        }
    }

    // ─── Session control ──────────────────────────────────────────────────────

    fun startSession(user: String) {
        viewModelScope.launch {
            val sessionId = stockTakeRepository.startSession(user)
            observeCountedIds(sessionId)
            _uiState.value = _uiState.value.copy(
                message = "Stock take session started"
            )
        }
    }

    fun closeSession() {
        val sessionId = _uiState.value.openSession?.sessionId ?: return
        viewModelScope.launch {
            stockTakeRepository.closeSession(sessionId)
            _uiState.value = _uiState.value.copy(
                openSession = null,
                countedProductIds = emptySet(),
                checkedCount = 0,
                message = "Stock take closed successfully"
            )
        }
    }

    fun cancelSession() {
        val sessionId = _uiState.value.openSession?.sessionId ?: return
        viewModelScope.launch {
            stockTakeRepository.cancelSession(sessionId)
            _uiState.value = _uiState.value.copy(
                openSession = null,
                countedProductIds = emptySet(),
                checkedCount = 0,
                message = "Stock take cancelled"
            )
        }
    }

    // ─── Item recording ───────────────────────────────────────────────────────

    /**
     * Saves a stock-take record immediately to Room.
     * Called on every "Check" button tap — no buffering.
     */
    fun recordItem(
        product: Products,
        countedStock: Double,
        varianceReason: String,
        recordedBy: String
    ) {
        val sessionId = _uiState.value.openSession?.sessionId ?: return
        val variance = countedStock - product.productQty
        viewModelScope.launch {
            stockTakeRepository.upsertStockTakeRecord(
                StockTake(
                    sessionId = sessionId,
                    productId = product.productId,
                    recordedStock = product.productQty,
                    countedStock = countedStock,
                    variance = variance,
                    varianceReason = varianceReason.ifBlank { null },
                    dateRecorded = System.currentTimeMillis(),
                    recordedBy = recordedBy
                )
            )
        }
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // ─── New product dialog ───────────────────────────────────────────────────

    fun showAddProductDialog() {
        _uiState.value = _uiState.value.copy(showAddProductDialog = true)
    }

    fun dismissAddProductDialog() {
        _uiState.value = _uiState.value.copy(showAddProductDialog = false)
    }

    /**
     * Inserts a new product into the Products table, creates a price list entry,
     * then immediately records it as checked in the current session.
     */
    fun addNewProductAndRecord(
        description: String,
        productCode: String,
        brandId: Long,
        shoeSizeId: Long,
        averageCost: Double,
        standardCost: Double,
        countedQty: Double,
        recordedBy: String
    ) {
        val sessionId = _uiState.value.openSession?.sessionId ?: return
        viewModelScope.launch {
            try {
                val newProduct = Products(
                    productDescription = description.trim(),
                    productQty = 0L,               // system qty = 0 (new item)
                    brandID = brandId,
                    shoeSizeId = shoeSizeId,
                    saleable = true,
                    purchasable = true,
                    averageCost = averageCost,
                    standardCost = standardCost,
                    salesTaxCode = null,
                    productCode = productCode.trim(),
                    inclusiveTaxed = false,
                    lastUpdated = System.currentTimeMillis()
                )
                val productId = productRepository.insertProduct(newProduct)

                // Insert price list entry
                priceListRepository.insertPriceLists(
                    listOf(
                        PriceList(
                            productId = productId,
                            productDescription = description.trim(),
                            unitCost = standardCost,
                            sellingPrice = standardCost,
                            markupPercentage = 0.0,
                            grossProfitPercentage = 0.0
                        )
                    )
                )

                // Immediately record in the current session
                stockTakeRepository.upsertStockTakeRecord(
                    StockTake(
                        sessionId = sessionId,
                        productId = productId,
                        recordedStock = 0L,
                        countedStock = countedQty,
                        variance = countedQty,   // system was 0, all is variance
                        varianceReason = "New product added during stock take",
                        dateRecorded = System.currentTimeMillis(),
                        recordedBy = recordedBy
                    )
                )

                _uiState.value = _uiState.value.copy(
                    showAddProductDialog = false,
                    message = "\"$description\" added and recorded"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Failed to add product: ${e.message}"
                )
            }
        }
    }

    // ─── Message consumed ─────────────────────────────────────────────────────

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
