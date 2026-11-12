package com.ipos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipos.data.domain.ProductWithBrand
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.data.entities.StockTake
import com.ipos.data.entities.StockTakeSession
import com.ipos.data.repositories.PriceListRepository
import com.ipos.data.repositories.ProductRepository
import com.ipos.data.repositories.ShoeBrandRepository
import com.ipos.data.repositories.ShoeSizeRepository
import com.ipos.data.repositories.StockTakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

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
    private val priceListRepository: PriceListRepository,
    private val shoeBrandRepository: ShoeBrandRepository,
    private val shoeSizeRepository: ShoeSizeRepository
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
     * Creates a complete shoe brand with sizes 3-11, all products, price lists,
     * and records them in the current stock take session.
     * 
     * Workflow:
     * 1. Check if brand exists by name (case-insensitive), else create it
     * 2. Create 9 ShoeSize entries (sizes 3-11) with qty=100 each
     * 3. Create 9 Products linking brand + each size with description + costs
     * 4. Create 9 PriceList entries
     * 5. Record all 9 products in the current stock take session
     */
    fun addNewBrandWithProductsAndRecord(
        brandName: String,
        productDescription: String,
        averageCost: Double,
        standardCost: Double,
        countedQtyPerSize: Double,
        recordedBy: String
    ) {
        val sessionId = _uiState.value.openSession?.sessionId ?: return
        viewModelScope.launch {
            try {
                // Step 1: Resolve or create brand
                val brand = shoeBrandRepository.findBrandByNameCaseInsensitive(brandName.trim())
                    ?: run {
                        val newBrandId = shoeBrandRepository.insertShoeBrand(
                            com.ipos.data.entities.ShoeBrand(
                                brandName = brandName.trim()
                            )
                        )
                        shoeBrandRepository.getShoeBrandById(newBrandId)!!
                    }
                val brandId = brand.brandID

                // Step 2: Create shoe sizes 3-11 with default qty=100
                val shoeSizeIds = mutableListOf<Long>()
                for (size in 3..11) {
                    // Check if size already exists for this brand
                    val existingSize = shoeSizeRepository.findSizeByBrandAndValue(brandId, size.toDouble())
                    val sizeId = if (existingSize != null) {
                        existingSize.shoeSizeId
                    } else {
                        shoeSizeRepository.insertShoeSize(
                            com.ipos.data.entities.ShoeSize(
                                brandID = brandId,
                                size = size.toDouble(),
                                quantity = 100L
                            )
                        )
                    }
                    shoeSizeIds.add(sizeId)
                }

                // Step 3-5: Create products, price lists, and stock take records for each size
                var createdCount = 0
                shoeSizeIds.forEachIndexed { index, shoeSizeId ->
                    val size = (3 + index).toDouble()
                    
                    // Check if product already exists
                    val existingProduct = productRepository.getProductByExactMatch(
                        brandId = brandId,
                        shoeSizeId = shoeSizeId,
                        description = productDescription.trim()
                    )
                    
                    val productId = if (existingProduct != null) {
                        existingProduct.productId
                    } else {
                        // Create new product
                        val newProduct = Products(
                            productDescription = productDescription.trim(),
                            productQty = 100L,  // Default stock level
                            brandID = brandId,
                            shoeSizeId = shoeSizeId,
                            saleable = true,
                            purchasable = true,
                            averageCost = averageCost,
                            standardCost = standardCost,
                            salesTaxCode = null,
                            productCode = "",  // Will be auto-generated by repository
                            inclusiveTaxed = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                        val id = productRepository.insertProduct(newProduct)
                        
                        // Create price list entry
                        priceListRepository.insertPriceLists(
                            listOf(
                                PriceList(
                                    productId = id,
                                    productDescription = productDescription.trim(),
                                    unitCost = standardCost,
                                    sellingPrice = standardCost,
                                    markupPercentage = 0.0,
                                    grossProfitPercentage = 0.0
                                )
                            )
                        )
                        createdCount++
                        id
                    }

                    // Record in stock take session
                    stockTakeRepository.upsertStockTakeRecord(
                        StockTake(
                            sessionId = sessionId,
                            productId = productId,
                            recordedStock = if (existingProduct != null) existingProduct.productQty else 100L,
                            countedStock = countedQtyPerSize,
                            variance = countedQtyPerSize - (if (existingProduct != null) existingProduct.productQty else 100L),
                            varianceReason = if (existingProduct == null) "New product added during stock take" else null,
                            dateRecorded = System.currentTimeMillis(),
                            recordedBy = recordedBy
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    showAddProductDialog = false,
                    message = "\"${brandName.trim()}\" added: $createdCount new products (sizes 3-11) recorded"
                )
            } catch (e: Exception) {
                android.util.Log.e("StockTakeVM", "Failed to add brand with products", e)
                _uiState.value = _uiState.value.copy(
                    message = "Failed to add brand: ${e.message}"
                )
            }
        }
    }

    // ─── Message consumed ─────────────────────────────────────────────────────

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // ─── Session detail ───────────────────────────────────────────────────────

    private val _detailState = MutableStateFlow<StockTakeDetailState>(StockTakeDetailState.Loading)
    val detailState: StateFlow<StockTakeDetailState> = _detailState.asStateFlow()

    fun loadSessionDetail(sessionId: Long) {
        viewModelScope.launch {
            _detailState.value = StockTakeDetailState.Loading
            try {
                // Fetch session meta
                val session = stockTakeRepository.getAllSessions().first()
                    .find { it.sessionId == sessionId }
                    ?: run {
                        _detailState.value = StockTakeDetailState.Error("Session not found")
                        return@launch
                    }

                // Fetch all stock-take records for this session
                val records = stockTakeRepository.getRecordsForSession(sessionId).first()

                // Fetch all products with brand for name lookup
                val allProducts = productRepository.getProductsWithBrand().first()
                val productMap: Map<Long, ProductWithBrand> =
                    allProducts.associateBy { it.product.productId }

                // Total catalog size
                val totalCatalog = allProducts.size

                // Counted = products that have a record in this session
                val countedCount = records.size

                // Variances
                val withVariance = records.filter { abs(it.variance) > 0.0 }
                val shortages = withVariance.filter { it.variance < 0 }
                val surpluses = withVariance.filter { it.variance > 0 }

                // Coverage %
                val coveragePct = if (totalCatalog > 0)
                    (countedCount.toFloat() / totalCatalog * 100).toInt() else 0

                // Uncounted products
                val countedIds = records.map { it.productId }.toSet()
                val uncounted = allProducts.filter { it.product.productId !in countedIds }

                // Variance rows — sorted by absolute variance desc
                val varianceRows = withVariance
                    .sortedByDescending { abs(it.variance) }
                    .map { record ->
                        val pwb = productMap[record.productId]
                        VarianceRow(
                            displayName = pwb?.let { "${it.brand.brandName} - ${it.product.productDescription}" }
                                ?: "Product #${record.productId}",
                            systemQty = record.recordedStock,
                            countedQty = record.countedStock,
                            variance = record.variance,
                            reason = record.varianceReason
                        )
                    }

                // Business suggestions
                val suggestions = buildSuggestions(
                    coveragePct = coveragePct,
                    totalCatalog = totalCatalog,
                    countedCount = countedCount,
                    shortageCount = shortages.size,
                    surplusCount = surpluses.size,
                    varianceCount = withVariance.size
                )

                _detailState.value = StockTakeDetailState.Ready(
                    StockTakeDetail(
                        session = session,
                        totalCatalog = totalCatalog,
                        countedCount = countedCount,
                        coveragePct = coveragePct,
                        varianceCount = withVariance.size,
                        shortageCount = shortages.size,
                        surplusCount = surpluses.size,
                        uncountedProducts = uncounted,
                        varianceRows = varianceRows,
                        suggestions = suggestions
                    )
                )
            } catch (e: Exception) {
                _detailState.value = StockTakeDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun buildSuggestions(
        coveragePct: Int,
        totalCatalog: Int,
        countedCount: Int,
        shortageCount: Int,
        surplusCount: Int,
        varianceCount: Int
    ): List<StockTakeSuggestion> {
        val list = mutableListOf<StockTakeSuggestion>()

        if (coveragePct < 50) {
            list += StockTakeSuggestion(
                type = SuggestionType.WARNING,
                title = "Low coverage ($coveragePct%)",
                body = "Less than half your catalog was counted. Avoid making purchasing or pricing decisions until the remaining ${totalCatalog - countedCount} products are checked."
            )
        } else if (coveragePct < 80) {
            list += StockTakeSuggestion(
                type = SuggestionType.INFO,
                title = "Partial coverage ($coveragePct%)",
                body = "${totalCatalog - countedCount} products still uncounted. Consider completing the count before closing the session."
            )
        } else {
            list += StockTakeSuggestion(
                type = SuggestionType.OK,
                title = "Good coverage ($coveragePct%)",
                body = "Most of your catalog has been counted. Results are reliable for decision-making."
            )
        }

        val shortageRate = if (countedCount > 0) shortageCount.toFloat() / countedCount else 0f
        if (shortageRate > 0.10f) {
            list += StockTakeSuggestion(
                type = SuggestionType.WARNING,
                title = "High shortage rate (${(shortageRate * 100).toInt()}%)",
                body = "$shortageCount products have less stock than recorded. Review receiving records and check for possible shrinkage, theft, or unprocessed sales."
            )
        } else if (shortageCount > 0) {
            list += StockTakeSuggestion(
                type = SuggestionType.INFO,
                title = "$shortageCount shortage${if (shortageCount > 1) "s" else ""} found",
                body = "Cross-check these items against recent sales invoices to identify any unrecorded transactions."
            )
        }

        if (surplusCount > 0) {
            list += StockTakeSuggestion(
                type = SuggestionType.INFO,
                title = "$surplusCount surplus${if (surplusCount > 1) "s" else ""} found",
                body = "Stock exceeds system records for these items. Check if all received deliveries have been captured in the system."
            )
        }

        if (varianceCount == 0 && countedCount > 0) {
            list += StockTakeSuggestion(
                type = SuggestionType.OK,
                title = "Perfect match",
                body = "All counted products match system records exactly. Your inventory data is accurate."
            )
        }

        return list
    }
}

// ─── Detail data models ───────────────────────────────────────────────────────

data class VarianceRow(
    val displayName: String,
    val systemQty: Long,
    val countedQty: Double,
    val variance: Double,
    val reason: String?
)

data class StockTakeSuggestion(
    val type: SuggestionType,
    val title: String,
    val body: String
)

enum class SuggestionType { OK, INFO, WARNING }

data class StockTakeDetail(
    val session: StockTakeSession,
    val totalCatalog: Int,
    val countedCount: Int,
    val coveragePct: Int,
    val varianceCount: Int,
    val shortageCount: Int,
    val surplusCount: Int,
    val uncountedProducts: List<ProductWithBrand>,
    val varianceRows: List<VarianceRow>,
    val suggestions: List<StockTakeSuggestion>
)

sealed class StockTakeDetailState {
    object Loading : StockTakeDetailState()
    data class Ready(val detail: StockTakeDetail) : StockTakeDetailState()
    data class Error(val message: String) : StockTakeDetailState()
}
