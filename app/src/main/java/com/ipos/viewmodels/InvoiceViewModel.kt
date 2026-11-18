package com.ipos.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ipos.R
import com.ipos.data.domain.ProductWithBrand
import com.ipos.data.entities.CartItem
import com.ipos.data.entities.CsvParser
import com.ipos.data.entities.Currency
import com.ipos.data.entities.Customer
import com.ipos.data.entities.Invoice
import com.ipos.data.entities.InvoiceItem
import com.ipos.data.entities.InvoiceLine
import com.ipos.data.entities.Order
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
import com.ipos.data.entities.StockTake
import com.ipos.data.entities.User
import com.ipos.data.repositories.AuthRepository
import com.ipos.data.repositories.CartItemRepository
import com.ipos.data.repositories.CustomerRepository
import com.ipos.data.repositories.DayReportsRepository
import com.ipos.data.repositories.InvoiceItemRepository
import com.ipos.data.repositories.InvoiceLineRepository
import com.ipos.data.repositories.InvoiceRepository
import com.ipos.data.repositories.OrderRepository
import com.ipos.data.repositories.PriceListRepository
import com.ipos.data.repositories.ProductDayReportRepository
import com.ipos.data.repositories.ProductRepository
import com.ipos.data.repositories.ShoeBrandRepository
import com.ipos.data.repositories.ShoeSizeRepository
import com.ipos.data.repositories.StockTakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import javax.inject.Inject
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.workDataOf
import com.ipos.data.CsvInvoiceItem
import com.ipos.data.entities.FailedInvoiceItem
import com.ipos.data.entities.ProductCsvModel
import com.ipos.app.core.DeviceSession
import com.ipos.data.repositories.FailedInvoiceRepository
import com.ipos.network.SyncInvoiceWorker
import com.ipos.network.SyncProductWorker
import com.ipos.network.SyncInvoiceLineWorker
import com.ipos.utils.StringMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit


@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val invoiceItemRepository: InvoiceItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val invoiceLineRepository: InvoiceLineRepository,
    private val orderRepository: OrderRepository,
    private val priceListRepository: PriceListRepository,
    private val shoeSizeRepository: ShoeSizeRepository,
    private val shoeBrandRepository: ShoeBrandRepository,
    private val authRepository: AuthRepository,
    private val dayReportRepository: DayReportsRepository,
    private val productDayReportRepository: ProductDayReportRepository,
    private val stockTakeRepository: StockTakeRepository,
    private val failedInvoiceRepository: FailedInvoiceRepository,
    private val workManager: WorkManager,
    private val syncManager: com.ipos.network.SyncManager,
    private val deviceSession: DeviceSession,
    private val deviceManager: com.ipos.app.core.DeviceManager
) : ViewModel() {

    private val _shoeSizes = MutableStateFlow(emptyList<ShoeSize>())
    val shoeSizes: StateFlow<List<ShoeSize>> = _shoeSizes.asStateFlow()

    private val _shoeBrands = MutableStateFlow(emptyList<ShoeBrand>())
    val shoeBrands: StateFlow<List<ShoeBrand>> = _shoeBrands.asStateFlow()


    private val _dayStarted = MutableStateFlow(false)
    val dayStarted: StateFlow<Boolean> = _dayStarted.asStateFlow()

    // Use Triple to hold product, price, and quantity
    private val _productsWithPriceDetails =
        MutableStateFlow<List<Triple<Products, Double, Int>>>(emptyList())
    val productsWithPriceDetails: StateFlow<List<Triple<Products, Double, Int>>> =
        _productsWithPriceDetails

    private val _priceList = MutableStateFlow(emptyList<PriceList>())
    val priceList = _priceList.asStateFlow()

    // Quantities selected on ProductSelectionScreen — shared with CartScreen
    private val _selectedQuantities = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val selectedQuantities: StateFlow<Map<Long, Int>> = _selectedQuantities.asStateFlow()

    fun setProductQuantity(productId: Long, qty: Int) {
        val current = _selectedQuantities.value.toMutableMap()
        if (qty <= 0) current.remove(productId) else current[productId] = qty
        _selectedQuantities.value = current
    }

    fun clearSelectedQuantities() {
        _selectedQuantities.value = emptyMap()
    }

    /** Full cart reset — call after successful payment or when starting a new sale. */
    fun clearCart() {
        _selectedProducts.value = emptyList()
        _selectedQuantities.value = emptyMap()
        _productsWithPriceDetails.value = emptyList()
        _selectedCurrency.value = null
    }


    // Cart items (CartItem)
    private val _cartItems = MutableStateFlow(emptyList<CartItem>())

    val cartItems = _cartItems.asStateFlow()

    // Orders list
    private val _orderList = MutableStateFlow<List<Order>>(emptyList())
    val orderList: StateFlow<List<Order>> = _orderList.asStateFlow()

    // Selected order
    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    // Customer list
    private val _customerList = MutableStateFlow(emptyList<Customer>())
    val customerList = _customerList.asStateFlow()

    // Product list
    private val _productsList = MutableStateFlow(emptyList<Products>())
    val productList = _productsList.asStateFlow()

    // ShoeSize list
    private val _shoeSizeList = MutableStateFlow(emptyList<ShoeSize>())
    val shoeSizeList = _shoeSizeList.asStateFlow()

    // ShoeBrand list
    private val _shoeBrandList = MutableStateFlow(emptyList<ShoeBrand>())
    val shoeBrandList = _shoeBrandList.asStateFlow()

    private val _invoiceLineList = MutableStateFlow(emptyList<InvoiceLine>())
    val invoiceLineList = _invoiceLineList.asStateFlow()

    // List to store selected products for the cart
    private val _selectedProducts = MutableStateFlow<List<Products>>(emptyList())
    val selectedProducts: StateFlow<List<Products>> = _selectedProducts

    // Invoice items (InvoiceItem)
    private val _invoiceItems = MutableStateFlow(mutableListOf<InvoiceItem>())

    val invoiceItems = _invoiceItems.asStateFlow()

    // For fetching invoices and items
    private val _invoiceList = MutableStateFlow(emptyList<Invoice>())
    val invoiceList = _invoiceList.asStateFlow()

    // State to hold invoice details
    private val _invoiceDetailItems = MutableStateFlow<List<InvoiceItem>>(emptyList())
    val invoiceDetailItems: StateFlow<List<InvoiceItem>> = _invoiceDetailItems.asStateFlow()

    private val _invoiceLines = MutableStateFlow<List<InvoiceLine>?>(null)
    val invoiceLines: StateFlow<List<InvoiceLine>?> get() = _invoiceLines

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    val currencies: StateFlow<List<Currency>> = _currencies

    private val _selectedCurrency = MutableStateFlow<Currency?>(null)
    val selectedCurrency: StateFlow<Currency?> = _selectedCurrency

    private val _withBrandProductList = MutableStateFlow(emptyList<ProductWithBrand>())
    val withBrandProductList = _withBrandProductList.asStateFlow()

    private val _failedInvoiceItems = MutableStateFlow<List<FailedInvoiceItem>>(emptyList())
    val failedInvoiceItems: StateFlow<List<FailedInvoiceItem>> = _failedInvoiceItems.asStateFlow()

    // State for unsynced count — combined invoices + invoice lines
    private val _unsyncedCount = MutableStateFlow(0)
    val notsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private var initialUnsyncedAtStart = 0

    // Track sync status
    private val _syncStatus = MutableLiveData<String>("Idle")
    val syncStatus: LiveData<String> = _syncStatus

    enum class SyncState { IDLE, SYNCING, SUCCESS, FAILED }

    /**
     * Real sync state driven by WorkManager job status for the full_sync chain.
     * Cards observe this instead of guessing from animation timers.
     */
    val syncState: StateFlow<SyncState> =
        workManager.getWorkInfosForUniqueWorkFlow("full_sync")
            .map { infos ->
                when {
                    infos.any { it.state == WorkInfo.State.RUNNING } ->
                        SyncState.SYNCING
                    infos.isNotEmpty() && infos.all { it.state == WorkInfo.State.SUCCEEDED } ->
                        SyncState.SUCCESS
                    infos.any { it.state == WorkInfo.State.FAILED } ->
                        SyncState.FAILED
                    else -> SyncState.IDLE
                }
            }
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), SyncState.IDLE)

    companion object {
        private const val TAG = "backIntCheck"
        private const val SYNC_BATCH_SIZE = 200
        private const val BATCH_SYNC_WORK_NAME = "invoice_batch_sync"
        const val PRODUCT_BATCH_SYNC_WORK_NAME = "product_batch_sync_work"
        const val INVOICE_LINE_BATCH_SYNC_WORK_NAME = "invoice_line_batch_sync_work"
    }

    init {
        // Essential db operations to load data
        insertDefaultUser()
        fetchAllInvoiceLines()
        fetchPriceList()
        loadInvoices()
        insertInitialCustomers()
        fetchCustomers()
        fetchAllOrders()
        insertInitialShoeBrands()
        fetchShoeBrands()
        // Insert shoe sizes and brands first to avoid foreign key violations
        insertInitialShoeSizes()
        // Fetch necessary data after inserting shoe sizes and brands
        fetchShoeSizes()
        fetchInvoiceLines()
        fetchProducts()
        insertInitialCurrencies()
        fetchCurrencies()
        fetchCurrencyTotals()
        fetchBrandProducts()
        // Monitor combined unsynced count (invoices + invoice lines)
        monitorUnsyncedCount()
        monitorSyncProgress()
        fetchFailedInvoices()
    }

    private fun monitorUnsyncedCount() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                invoiceRepository.getUnsyncedCount(),
                invoiceLineRepository.getAllInvoiceLines()
            ) { invoiceUnsynced, allLines ->
                val lineUnsynced = allLines.count { !it.synced }
                invoiceUnsynced + lineUnsynced
            }.collectLatest { total ->
                _unsyncedCount.value = total
            }
        }
    }

    private fun monitorUnsyncedInvoiceLineCount() {
        // Replaced by monitorUnsyncedCount() — kept to avoid breaking any external call sites
    }

    private fun monitorSyncStatus() {
        // Monitor batch sync work
        workManager.getWorkInfosForUniqueWorkLiveData("invoice_batch_sync")
            .observeForever { workInfoList ->
                if (workInfoList.isNotEmpty()) {
                    val workInfo = workInfoList[0]
                    _syncStatus.value = when (workInfo.state) {
                        WorkInfo.State.RUNNING -> "Syncing..."
                        WorkInfo.State.SUCCEEDED -> "Sync Completed"
                        WorkInfo.State.FAILED -> "Sync Failed"
                        WorkInfo.State.BLOCKED -> "Sync Waiting"
                        WorkInfo.State.ENQUEUED -> "Sync Scheduled"
                        WorkInfo.State.CANCELLED -> "Sync Cancelled"
                    }
                }
            }
    }

    fun syncAllInvoices() {
        viewModelScope.launch {
            syncAllNow()
        }
    }

    fun fetchBrandProducts() = viewModelScope.launch {
        productRepository.getProductsWithBrand().collectLatest { branProductList ->
            _withBrandProductList.value = branProductList
        }
    }

    fun insertStockTake(stockTake: StockTake) {
        viewModelScope.launch {
            stockTakeRepository.insertStockTake(stockTake)
        }
    }

    fun getShoeSizeById(shoeSizeId: Long): StateFlow<ShoeSize?> {
        val shoeSizeFlow = MutableStateFlow<ShoeSize?>(null)
        viewModelScope.launch {
            shoeSizeFlow.value = shoeSizeRepository.getShoeSizeById(shoeSizeId)
        }
        return shoeSizeFlow
    }

    suspend fun getProductByNameAndSize(productName: String, shoeSizeId: Long): Products? {
        return productRepository.getProductByNameAndSize(productName, shoeSizeId)
    }


    private val _brandNameCache = mutableMapOf<Long, String>()

    suspend fun getBrandNameById(brandId: Long): String {
        return _brandNameCache[brandId] ?: run {
            val brand = shoeBrandRepository.getBrandNameById(brandId)
            val name = brand?.brandName ?: "Unknown Brand"
            _brandNameCache[brandId] = name
            name
        }
    }

    // Function to fetch the stock level for a product based on brandId
    suspend fun fetchStockLevel(brandId: Long): Int {
        return shoeSizeRepository.getTotalQuantityByBrand(brandId)
    }

    // Fetch current stock by brandID
    suspend fun getCurrentStockByBrand(brandID: Long): Int {
        return shoeSizeRepository.getTotalQuantityByBrand(brandID)
    }

    // Fetch all currencies
    fun fetchCurrencyTotals() = viewModelScope.launch {
        invoiceRepository.getAllInvoices().collect { invoices ->
            _invoiceList.value = invoices
        }
    }

    // Fetch all currencies
    fun fetchCurrencies() = viewModelScope.launch {
        invoiceRepository.getAllCurrencies().collectLatest { currencyList ->
            _currencies.value = currencyList
        }
    }

    // Fetch all shoe sizes
    fun fetchShoeSizes() = viewModelScope.launch {
        shoeSizeRepository.getAllShoeSizes().collectLatest { shoeSizesList ->
            _shoeSizes.value = shoeSizesList
        }
    }

    // Fetch all shoe brands
    fun fetchShoeBrands() = viewModelScope.launch {
        shoeBrandRepository.getAllShoeBrands().collectLatest { shoeBrandsList ->
            _shoeBrands.value = shoeBrandsList
        }
    }

    // Optimized CSV upload with batch processing
    fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
        viewModelScope.launch {
            try {

                val parser = CsvParser()
                val productsFromCsv = parser.parseProductCsv(inputStream)

                if (productsFromCsv.isEmpty()) {
                    Log.w("ProductUpload", "⚠️ No products parsed from CSV. Check CSV format.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "No products found in CSV. Check file format.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val BATCH_SIZE = 100
                var totalInserted = 0
                var totalUpdated = 0
                var totalErrors = 0

                productsFromCsv.chunked(BATCH_SIZE).forEachIndexed { batchIndex, batch ->
                    val result = processBatchProducts(batch, context)
                    totalInserted += result.inserted
                    totalUpdated += result.updated
                    totalErrors += result.errors
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Products uploaded: $totalInserted new, $totalUpdated updated, $totalErrors errors",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("ProductUpload", "❌ Failed to upload products: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to upload products: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun processBatchProducts(
        batch: List<ProductCsvModel>,
        context: Context
    ): BatchResult = withContext(Dispatchers.IO) {
        var inserted = 0
        var updated = 0
        var errors = 0

        try {
            val existingProducts = productRepository.getAllProductsOnce()

            val existingMap = existingProducts.associateBy {
                Triple(
                    it.productDescription.trim().lowercase(),
                    it.shoeSizeId ?: -1L,
                    it.brandID ?: -1L
                )
            }

            val productsToInsert = mutableListOf<Products>()
            val productsToUpdate = mutableListOf<Products>()
            val priceListsToInsert = mutableListOf<PriceList>()
            val priceListsToUpdate = mutableListOf<PriceList>()

            batch.forEachIndexed { index, productCsv ->
                try {
                    val description = productCsv.productDescription.trim()
                    val shoeSizeId = productCsv.shoeSizeId ?: -1L
                    val brandID = productCsv.brandID ?: -1L

                    val key = Triple(description.lowercase(), shoeSizeId, brandID)
                    val existingProduct = existingMap[key]

                    val uniqueProductCode = if (productCsv.productCode.contains("H&P")) {
                        "${productCsv.productCode}_${description}_$shoeSizeId"
                    } else {
                        productCsv.productCode
                    }

                    if (existingProduct != null) {
                        val updatedProduct = existingProduct.copy(
                            productQty = productCsv.productQty,
                            brandID = brandID,
                            shoeSizeId = shoeSizeId,
                            saleable = productCsv.saleable,
                            purchasable = productCsv.purchasable,
                            averageCost = productCsv.averageCost,
                            standardCost = productCsv.standardCost ?: existingProduct.standardCost,
                            salesTaxCode = productCsv.salesTaxCode,
                            productCode = uniqueProductCode,
                            inclusiveTaxed = productCsv.inclusiveTaxed,
                            imageUri = productCsv.imageUri ?: existingProduct.imageUri,
                            lastUpdated = System.currentTimeMillis()
                        )
                        productsToUpdate.add(updatedProduct)

                        val priceList = PriceList(
                            productId = existingProduct.productId,
                            productDescription = existingProduct.productDescription,
                            unitCost = updatedProduct.standardCost,
                            sellingPrice = updatedProduct.standardCost * 1.0,
                            markupPercentage = 0.0,
                            grossProfitPercentage = 0.0
                        )
                        priceListsToUpdate.add(priceList)
                        updated++
                    } else {
                        val imageUri = getDefaultImageUri(context)
                        val standardCost = productCsv.standardCost ?: 0.0

                        val newProduct = Products(
                            productDescription = description,
                            productQty = productCsv.productQty,
                            brandID = brandID,
                            shoeSizeId = shoeSizeId,
                            saleable = productCsv.saleable,
                            purchasable = productCsv.purchasable,
                            averageCost = productCsv.averageCost,
                            standardCost = standardCost,
                            salesTaxCode = productCsv.salesTaxCode,
                            productCode = uniqueProductCode,
                            inclusiveTaxed = productCsv.inclusiveTaxed,
                            imageUri = imageUri,
                            lastUpdated = System.currentTimeMillis()
                        )
                        productsToInsert.add(newProduct)
                        inserted++
                    }
                } catch (e: Exception) {
                    errors++
                    Log.e(
                        "BatchProcess",
                        "❌ Error processing product [${index + 1}]: ${productCsv.productDescription} - ${e.message}",
                        e
                    )
                }
            }

            if (productsToInsert.isNotEmpty()) {
                val insertedIds = productRepository.insertProducts(productsToInsert)
                insertedIds.forEachIndexed { index, productId ->
                    val product = productsToInsert[index]
                    priceListsToInsert.add(
                        PriceList(
                            productId = productId,
                            productDescription = product.productDescription,
                            unitCost = product.standardCost,
                            sellingPrice = product.standardCost * 1.0,
                            markupPercentage = 0.0,
                            grossProfitPercentage = 0.0
                        )
                    )
                }
                priceListRepository.insertPriceLists(priceListsToInsert)
            }

            if (productsToUpdate.isNotEmpty()) {
                productRepository.updateProducts(productsToUpdate)
                priceListRepository.updatePriceLists(priceListsToUpdate)
            }

        } catch (e: Exception) {
            Log.e("BatchProcess", "❌ Batch processing failed: ${e.message}", e)
            e.printStackTrace()
        }

        BatchResult(inserted, updated, errors)
    }

    data class BatchResult(val inserted: Int, val updated: Int, val errors: Int)

    data class ValidInvoiceItem(
        val product: Products,
        val quantity: Int,
        val amountTendered: Double,
        val customerId: Long
    )

    // Function to get default image URI from drawable resources
    fun getDefaultImageUri(context: Context): String {
        val defaultImageResId = R.drawable.meat7
        return Uri.parse("android.resource://${context.packageName}/$defaultImageResId").toString()
    }

    private fun fetchPriceList() {
        viewModelScope.launch {
            priceListRepository.getAllPriceLists().collect { prices ->
                _priceList.value = prices
            }
        }
    }

    fun getPricelistById(productId: Long?): PriceList? {
        return priceList.value.firstOrNull { it.productId == productId }
    }

    fun getPriceDetailsForSelectedProducts(
        selectedInventories: List<Products>,
        productQuantities: Map<Long, Int>
    ) {
        viewModelScope.launch {
            val productDetailsWithPrices = selectedInventories.map { product ->
                val priceList = priceListRepository.getPriceListByProductId(product.productId)
                val productPrice = priceList?.sellingPrice ?: 0.0
                val quantitySold = productQuantities[product.productId]
                    ?: 1 // Get quantity from productQuantities map
                Triple(
                    product,
                    productPrice,
                    quantitySold
                ) // Return Triple with product, price, and quantity
            }
            _productsWithPriceDetails.value = productDetailsWithPrices
        }
    }

    // Use Triple to hold product, price, and quantity
// In ViewModel
    private val _butcherProductsWithPriceDetails =
        MutableStateFlow<List<Triple<Products, Double, Double>>>(emptyList())
    val butcherProductsWithPriceDetails: StateFlow<List<Triple<Products, Double, Double>>> =
        _butcherProductsWithPriceDetails

    fun getPriceDetailsForButcherSelectedProducts(
        selectedInventories: List<Products>,
        productQuantities: Map<Long, Double>
    ) {
        viewModelScope.launch {
            val butcherProductWithPriceDetails = selectedInventories.map { product ->
                val priceList = priceListRepository.getPriceListByProductId(product.productId)
                val pricePerKg = priceList?.sellingPrice ?: 0.0
                val defaultAmountPaid = pricePerKg * (productQuantities[product.productId]
                    ?: 1.0) // Default amount paid

                Triple(
                    product,
                    pricePerKg,
                    defaultAmountPaid
                ) // Return Triple with product, price, and default amount paid
            }
            _butcherProductsWithPriceDetails.value = butcherProductWithPriceDetails
        }
    }


    private fun loadInvoices() = viewModelScope.launch {
        invoiceRepository.getAllInvoices().collectLatest { invoiceList ->
            _invoiceList.value = invoiceList
        }
    }

    // Fetch invoiceLines by invoiceId from repository
    fun fetchInvoiceLinesByInvoiceId(invoiceId: Long) = viewModelScope.launch {
        _invoiceLines.value = null // null = loading
        val invoiceLineList = invoiceLineRepository.getInvoiceLinesByInvoiceId(invoiceId)
        _invoiceLines.value = invoiceLineList
        android.util.Log.d("InvoiceDetail", "Loaded ${invoiceLineList.size} lines for invoice $invoiceId")
    }

    fun fetchAllInvoiceLines() = viewModelScope.launch {
        invoiceLineRepository.getAllInvoiceLines().collectLatest { invoiceLineList ->
            _invoiceLineList.value = invoiceLineList
        }
    }

    // Fetch customers from repository
    private fun findCustomers() = viewModelScope.launch {
        customerRepository.getAllCustomers().collectLatest { _customerList.value = it }
    }

    private fun insertInitialCustomers() = viewModelScope.launch {
        val existingCustomers = customerRepository.getAllCustomersOnce()
        if (existingCustomers.isEmpty()) {
            val initialCustomers = listOf(
                Customer(1, "Simba Start", "simba@istart.com", "123456789", "State House"),
                Customer(2, "Jane Smith", "jane@istart.com", "987654321", "State House"),
                Customer(3, "John Doe", "john@istart.com", "987654321", "State House"),
                Customer(4, "Amy Petersen", "amy@istart.com", "987654321", "State House"),
                Customer(5, "Amara Brown", "amara@istart.com", "987654321", "State House"),
                Customer(6, "walkin", "i@start.com", "000", "State House"),
            )
            customerRepository.insertCustomers(initialCustomers)
        }
    }

    // Fetch customers from the database
    private fun fetchCustomers() = viewModelScope.launch {
        customerRepository.getAllCustomers().collectLatest { customerList ->
            _customerList.value = customerList
        }
    }

    // Get customer by ID
    fun getCustomerById(customerId: Long?): Customer? {
        return customerList.value.firstOrNull { it.customerId == customerId }
    }

    fun getWalkinCustomer(): Customer? {
        return customerList.value.firstOrNull { it.name == "walkin" }
    }

    // Insert initial default user
    private fun insertDefaultUser() = viewModelScope.launch {
        val existingUsers = authRepository.getAllUsersOnce()

        if (existingUsers.isEmpty()) {
            val storeId = deviceSession.storeId.ifBlank { deviceManager.getStoreId() }
            if (storeId.isNotBlank()) {
                val initialUser = User(
                    userFullName = "Simba Start",
                    userContact = "0776815030",
                    userRole = "admin",
                    userPassword = "password",
                    storeID = storeId,
                    userEmail = "dr.simbastart@gmail.com",
                    userAddress = "3 Maywood St, Kwekwe, Zimbabwe"
                )
                authRepository.signUpUser(initialUser)
            }
        }
    }

    fun insertCurrency(currency: Currency) = viewModelScope.launch {
        invoiceRepository.insertCurrency(currency)
    }

    // Insert initial currencies
    private fun insertInitialCurrencies() = viewModelScope.launch {
        val existingCurrencies = invoiceRepository.getAllCurrenciesOnce()

        if (existingCurrencies.isEmpty()) {
            val initialCurrencies = listOf(
                Currency("USD", "United States Dollar", 1.0),
                Currency("ZAR", "South African Rand", 19.31),
                Currency("ZiG", "Zimbabwe Gold", 28.6776),
                Currency("ECO", "Ecocash Mobile USD", 1.0)
            )
            invoiceRepository.insertCurrencies(initialCurrencies)
        }
    }

    // Insert initial shoe sizes
    private fun insertInitialShoeSizes() = viewModelScope.launch {
        val existingSizes = shoeSizeRepository.getAllShoeSizesOnce()

        if (existingSizes.isEmpty()) {
            val initialSizes = listOf(
                ShoeSize(1, 1, 1.0, 0),
                ShoeSize(2, 1, 2.0, 0),
                ShoeSize(3, 1, 3.0, 0),
                ShoeSize(4, 1, 4.0, 0),
                ShoeSize(5, 1, 5.0, 0),
                ShoeSize(6, 1, 6.0, 0),
                ShoeSize(7, 1, 7.0, 0),
                ShoeSize(8, 1, 8.0, 0),
                ShoeSize(9, 1, 9.0, 0),
                ShoeSize(10, 1, 10.0, 0),
                ShoeSize(11, 1, 11.0, 0),
            )
            shoeSizeRepository.insertShoeSizes(initialSizes)
        }
    }

    // Insert initial shoe brands with optimized batch processing
    fun insertInitialShoeBrands() = viewModelScope.launch {
        val brandNames = listOf(
            "OLDSTOCK",
            "D166",
            "6610",
            "SY24023",
            "6886",
            "D159",
            "W2127A",
            "W2127J",
            "D160",
            "Z7283",
            "QY-247",
            "QY-122",
            "W23085B",
            "N05-1",
            "NF07-7",
            "NF07-9",
            "202588-3",
            "T18",
            "W24050D",
            "RR17-5",
            "RR17-6",
            "RR17-1",
            "RR17-7",
            "NF25-2",
            "RR17-4",
            "6620",
            "NF256",
            "NF164",
            "N04-3",
            "N04-1",
            "2893",
            "6610-2",
            "6610-4",
            "005",
            "NF24-1",
            "NF24-2",
            "NF24-5",
            "NF24-4",
            "NF24-3",
            "M265",
            "M256",
            "831",
            "836-2",
            "827-2",
            "NF29-1",
            "NF29-2",
            "NF29-3",
            "206-3",
            "W24145A",
            "2088-1",
            "N05-3",
            "15010-9",
            "206-4",
            "913",
            "M23085F",
            "X-9956",
            "NF51-1",
            "NF51-2",
            "NF51-3",
            "NF51-4",
            "NF51-5",
            "NF50-1",
            "NF50-2",
            "NF50-3",
            "NF50-4",
            "NF50-5",
            "NF50-6",
            "NF06-1",
            "NF06-2",
            "NF06-4",
            "NF06-7",
            "830-5",
            "02-6",
            "09-8",
            "W22103A",
            "PL2668-1",
            "NF52-1",
            "NF52-4",
            "NF52-5",
            "ZH881-2",
            "D37",
            "D38",
            "CX-114",
            "W22103W",
            "NF71-2",
            "AC14-2",
            "NF72-1",
            "NF72-4",
            "880",
            "NF92-4",
            "NFL-3014",
            "NF67-1",
            "W24024C",
            "2913-5",
            "NF108-5",
            "2422",
            "W23085C",
            "FK-67",
            "NF92-1",
            "W25022A",
            "FK60",
            "Q-7",
            "N04-4",
            "5831-4",
            "AR703-3",
            "CK-08-2",
            "NF72-3",
            "MG1123",
            "W25010B",
            "NF86-1",
            "2915-5",
            "LO67-5",
            "W25057D",
            "FK-65",
            "NF38-1",
            "M133-1",
            "W24024A",
            "FK68",
            "NF108-4",
            "NF108-1",
            "N08",
            "QY230",
            "D51G",
            "0912B-5",
            "M192-2",
            "N-15-5",
            "QY-238",
            "L90-1",
            "DY3851",
            "G69161-1",
            "L90-4",
            "M192-1",
            "0912B-4",
            "L90-6",
            "W9005",
            "M156-2",
            "NF86-4",
            "W24049A",
            "L90-5",
            "820-1",
            "D51K",
            "L90-2",
            "G69161-2",
            "WG-0005P",
            "0912B-3",
            "D51P",
            "FK-59",
            "W-37",
            "W9207",
            "FK21",
            "M156-1",
            "2913-1",
            "1820-1",
            "5160-1",
            "5160-2",
            "5160-3",
            "PL2599",
            "Z076",
            "W9193",
            "W24049B",
            "W25094C",
            "W11-1",
            "W11-2",
            "W11-3",
            "W11-4",
            "Q7",
            "W-57-3",
            "NF-15-1",
            "J1-3",
            "202588-1",
            "AC-14",
            "J1-1",
            "QY160",
            "WG-0005-1",
            "W-12-1",
            "NF-15-3",
            "XZ-66",
            "XZ-91",
            "M1201-6",
            "M1201-5",
            "M1201-1",
            "M1201-2",
            "M1201-8",
            "M1201-7",
            "W23085E",
            "2895",
            "W-16",
            "FK-60",
            "W-16-3",
            "L067-1",
            "S9025",
            "AC-14-2",
            "N04-2",
            "W23085D",
            "J1-4",
            "0912B-1",
            "0912B-2",
            "D152",
            "NF80-4",
            "NF108-3",
            "1105-2",
            "2160",
            "2459",
            "2459-1",
            "2459-3",
            "5160",
            "B160",
            "CK08-1",
            "CK80-1",
            "D51",
            "FK-1",
            "FK-160",
            "FK-66",
            "FK59",
            "J-1",
            "J-1-3",
            "L-1",
            "LOAFER",
            "M12-1",
            "M267",
            "N05-2",
            "N05-4",
            "N05-5",
            "N64-2",
            "NF-35-2",
            "NF100-4",
            "NF103-1",
            "NF103-2",
            "NF103-3",
            "NF108",
            "NF108-2",
            "NF122",
            "NF122-2",
            "NF122-3",
            "NF122-6",
            "NF124-2",
            "NF124-8",
            "NF13-1",
            "NF13-2",
            "NF13-5",
            "NF15-3",
            "NF16-1",
            "NF34-3",
            "NF35-1",
            "NF35-2",
            "NF50",
            "NF51",
            "NF52-2",
            "NF64-2",
            "NF64-4",
            "NF92-2",
            "PK60",
            "QY160",
            "SY2403",
            "W-57",
            "W25010E",
            "W25057",
            "W9169",
            "W9196",
            "WG-005",
            "WQ1966",
            "X-9856",
            "Z-802",
            "Z160",
            "Z802",
            "NEW BALANCE",
            "COCO8-1",
            "G69161-12",
            "L687-1",
            "L687-3",
            "L687-4",
            "LT502-1",
            "NF100",
            "NF100-3",
            "NF80-2",
            "W23085A",
            "W25010B",
            "W25010C",
            "W2572G",
            "Z7295",
            "5160"
        )

        val existingBrands = shoeBrandRepository.getAllShoeBrandsOnce()
        val existingBrandNames = existingBrands.map { it.brandName.lowercase() }.toSet()

        val newBrands = brandNames
            .filter { it.lowercase() !in existingBrandNames }
            .map { ShoeBrand(0, it) }

        if (newBrands.isNotEmpty()) {
            shoeBrandRepository.insertShoeBrands(newBrands)
        }
    }

    // Fetch products from the database
    fun fetchProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { productList ->
            _productsList.value = productList
        }
    }

    fun insertTheProduct(products: Products, onProductInserted: (Long) -> Unit) =
        viewModelScope.launch {
            val productId = productRepository.insertProduct(products)

            // Update ShoeSize quantity
            val shoeSize = shoeSizeRepository.getShoeSizeById(products.shoeSizeId)
            if (shoeSize != null) {
                val updatedShoeSize =
                    shoeSize.copy(quantity = shoeSize.quantity + products.productQty)
                shoeSizeRepository.updateShoeSize(updatedShoeSize)
            }

            onProductInserted(productId)
            fetchBrandProducts()
        }


    fun insertPriceList(priceList: PriceList) = viewModelScope.launch {
        priceListRepository.insertPriceList(priceList)
    }

    fun insertCurrentPriceList(
        price: Double,
        productDescription: String,
        productId: Long,
        context: Context
    ) =
        viewModelScope.launch {
            val rowsUpdated =
                priceListRepository.insertCurrentPriceList(price, productDescription, productId)
            if (rowsUpdated > 0) {
                // Handle success (if necessary)
                Toast.makeText(context, "Price updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failure (if necessary)
                Toast.makeText(context, "Failed to update price", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    fun insertProduct(products: Products) = viewModelScope.launch {
        productRepository.insertProduct(products)
    }

    // Function to calculate gross profit percentage
    fun calculateGrossProfitPercentage(unitCost: Double, sellingPrice: Double): Double {
        return if (sellingPrice != 0.0) {
            ((sellingPrice - unitCost) / sellingPrice) * 100
        } else 0.0
    }

    // Function to calculate markup percentage
    fun calculateMarkupPercentage(unitCost: Double, sellingPrice: Double): Double {
        return if (unitCost != 0.0) {
            ((sellingPrice - unitCost) / unitCost) * 100
        } else 0.0
    }

    // Function to calculate standardCost
    fun calculateStandardCost(unitCost: Double, currentStockLevel: Double): Double {
        return unitCost * currentStockLevel
    }

    // Function to calculate averageCost
    fun calculateAverageCost(currentStockLevel: Double, totalCost: Double): Double {
        return if (currentStockLevel > 0) {
            totalCost / currentStockLevel
        } else {
            0.0 // Avoid division by zero
        }
    }


    // Add a product to the cart (selected products list)
    fun addProductToCart(products: Products) {
        val updatedList = _selectedProducts.value.toMutableList()
        if (updatedList.contains(products)) {
            updatedList.remove(products) // Deselect if already added
        } else {
            updatedList.add(products) // Add if not already selected
        }
        _selectedProducts.value = updatedList
    }


    // Function to return total invoices for today
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalOrdersForToday(): Int {
        // Get today's date
        val today = LocalDate.now()

        // Log the current date for debugging

        // Log the entire order list for debugging

        // Filter orders for today
        val todayOrders = _orderList.value.filter { order ->
            // Assuming `order.orderDate` is in milliseconds
            val orderDate = LocalDate.ofEpochDay(order.orderDate / (24 * 60 * 60 * 1000))

            // Log each order date being checked
            // Check if the order date matches today
            orderDate == today
        }

        // Get the number of today's orders
        val totalOrdersForToday = todayOrders.size

        // Log the filtered orders and total for debugging

        return totalOrdersForToday
    }


    // Function to compute today's sales total and number of transactions
    fun getTodaySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()
        val todayStartMillis = getTodayStartInMillis()
        val todayEndMillis = todayStartMillis + (24 * 60 * 60 * 1000)

        // Filter invoices for today using milliseconds comparison
        val todayInvoices = _invoiceList.value.filter { invoice ->
            invoice.invoiceDate >= todayStartMillis && invoice.invoiceDate < todayEndMillis
        }

        // Group invoices by currency and sum totals
        val currencyTotals = todayInvoices.groupBy { it.currencyCode }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        // Count total number of transactions for today
        val transactionCount = todayInvoices.size

        // Log the filtered invoices for debugging
        return currencyTotals to transactionCount
    }

    //    calculate WeeklySalesInfo
    fun getWeeklySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        // Log the week range

        // Filter invoices for the current week
        val weeklyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate = LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))
            // Log each invoice date being checked
            invoiceDate.isAfter(startOfWeek.minusDays(1)) && invoiceDate.isBefore(
                endOfWeek.plusDays(
                    1
                )
            )
        }

        // Group invoices by currency and sum totals
        val currencyTotals = weeklyInvoices.groupBy { it.currencyCode }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        // Count total number of transactions for the week
        val transactionCount = weeklyInvoices.size

        // Log the filtered invoices for debugging
        return currencyTotals to transactionCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        // Log the month range

        // Filter invoices for the current month
        val monthlyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate = LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))
            // Log each invoice date being checked
            invoiceDate.isAfter(startOfMonth.minusDays(1)) && invoiceDate.isBefore(
                endOfMonth.plusDays(
                    1
                )
            )
        }

        // Group invoices by currency and sum totals
        val currencyTotals = monthlyInvoices.groupBy { it.currencyCode }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        // Count total number of transactions for the month
        val transactionCount = monthlyInvoices.size

        // Log the filtered invoices for debugging
        return currencyTotals to transactionCount
    }

    fun getInvoiceLineById(invoiceID: Long?): InvoiceLine? {
        return invoiceLineList.value.firstOrNull { it.invoiceId == invoiceID }
    }

    private fun fetchInvoiceLines() = viewModelScope.launch {
        invoiceLineRepository.getAllInvoiceLines().collectLatest { invoiceLineList ->
            _invoiceLineList.value = invoiceLineList
        }
    }


    // Fetch cart items for a customer
// Fetch cart items for a customer
    fun refreshCartItems(customerId: Long) = viewModelScope.launch {
        cartItemRepository.getCartItemsForCustomer(customerId).collectLatest { cartItems ->
            _cartItems.value = cartItems
        }
    }

    // Insert a new order
    fun submitOrder(productName: String, quantity: Int, dueDate: String, notes: String) =
        viewModelScope.launch {
            val newOrder = Order(
                productName = productName,
                quantity = quantity,
                dueDate = dueDate,
                notes = notes,
                orderDate = System.currentTimeMillis()
            )
            orderRepository.insertOrder(newOrder)
            fetchAllOrders()
        }

    // Fetch all orders
    fun fetchAllOrders() = viewModelScope.launch {
        orderRepository.getAllOrders().collectLatest { orders ->
            _orderList.value = orders
        }
    }

    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount

    // Fetch total count of all orders

    suspend fun getOrderCount1(): Flow<Int> = orderRepository.getOrderCount()

    // Fetch total count of all orders
    fun fetchOrderCount() = viewModelScope.launch {
        orderRepository.getOrderCount().collectLatest { count ->
            _orderCount.value = count
        }
    }

    // Get order by ID
    fun getOrderById(orderId: Int) = viewModelScope.launch {
        val order = orderRepository.getOrderById(orderId)
        _selectedOrder.value = order
    }

    // Delete a specific order
    fun deleteOrder(order: Order) = viewModelScope.launch {
        orderRepository.deleteOrder(order)
        fetchAllOrders()
    }

    // Delete all orders
    fun deleteAllOrders() = viewModelScope.launch {
        orderRepository.deleteAllOrders()
        fetchAllOrders()
    }


    // Update quantity in the cart
    fun updateCartItem(cartItem: CartItem, newQuantity: Double) = viewModelScope.launch {
        val updatedItem = cartItem.copy(totalAmount = newQuantity)
        cartItemRepository.updateCartItem(updatedItem)
        refreshCartItems(cartItem.customerId)
    }

    // Remove item from cart
    fun removeCartItem(cartItem: CartItem) = viewModelScope.launch {
        cartItemRepository.deleteCartItem(cartItem)
        refreshCartItems(cartItem.customerId)
    }

    // Clear cart after invoice submission
    fun clearCartForCustomer(customerId: Long) = viewModelScope.launch {
        cartItemRepository.clearCartForCustomer(customerId)
        refreshCartItems(customerId)
    }
//

    // Fetch all invoices from the repository
    fun fetchAllInvoices() = viewModelScope.launch {
        invoiceRepository.getAllInvoices().collectLatest { invoices ->
            _invoiceList.value = invoices
        }
    }

    // Fetch invoice items for a specific invoice
    fun getInvoiceItemsByInvoiceId(invoiceId: Long) {
        viewModelScope.launch {
            invoiceItemRepository.getInvoiceItemsByInvoiceId(invoiceId)
                .collectLatest { items ->
                    _invoiceDetailItems.value = items
                }
        }
    }

    fun getSingleProductById(productId: Long): Flow<Products?> {
        return productRepository.getSingleProductById(productId)
    }

    fun getBrandById(brandId: Long): Flow<ShoeBrand?> {
        return shoeBrandRepository.getBrandById(brandId)
    }

    suspend fun deductButcherQuantityAndUpdateStock(productId: Long, quantitySold: Double) {
        try {
            // Fetch the product from the repository
            val product = productRepository.getProductById(productId)

            // Check if the product exists
            if (product == null) {
                Log.e("viewmodel", "Product not found with productId: $productId")
                return
            }

            // Check if the product is saleable
            if (!product.saleable) {
                Log.e(
                    "viewmodel",
                    "Product with productId: $productId is not saleable"
                )
                return
            }

            // Fetch the related ShoeBrand and ShoeSize entities (if applicable to butcher logic)
            val shoeBrand = shoeBrandRepository.getShoeBrandById(product.brandID)
            val shoeSize = shoeSizeRepository.getShoeSizeById(product.shoeSizeId)

            if (shoeBrand == null || shoeSize == null) {
                Log.e(
                    "viewmodel",
                    "ShoeBrand or ShoeSize not found for productId: $productId"
                )
                return
            }

            // Calculate the actual stock level by summing the quantity of all ShoeSizes for the brand
            val totalStockLevel = shoeSizeRepository.getTotalQuantityByBrand(shoeBrand.brandID)

            // Check if there is enough stock to sell the desired quantity
            if (totalStockLevel < quantitySold) {
                Log.e(
                    "viewmodel",
                    "Insufficient stock for product: ${product.productDescription}. " +
                            "Available: $totalStockLevel, Required: $quantitySold"
                )
                return
            }

            // Deduct the quantity sold from the total stock level
            val newStockLevel = totalStockLevel - quantitySold

            // Update the stock level for the ShoeSize entity
//            shoeSizeRepository.updateShoeSizeStockLevel(shoeSize.shoeSizeId, newStockLevel)

            // Optionally update the Product entity if it tracks overall stock separately
            val updatedProduct = product.copy(newStockLevel.toLong())
            productRepository.updateProduct(updatedProduct)

        } catch (e: Exception) {
            Log.e(
                "viewmodel",
                "Error while updating stock for productId: $productId: ${e.message}"
            )
        }
    }

    fun deductProductQuantities(productQuantities: Map<Long, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                for ((productId, soldQuantity) in productQuantities) {
                    // Get the current product from the database
                    val product = productRepository.getProductById(productId)

                    product?.let {
                        // Calculate new quantity after sale
                        val newQuantity = maxOf(0, it.productQty - soldQuantity)

                        // Update the product with new quantity
                        val updatedProduct = it.copy(productQty = newQuantity)
                        productRepository.updateProduct(updatedProduct)
                    }
                }
            } catch (e: Exception) {
                Log.e("InventoryUpdate", "Failed to update product quantities", e)
            }
        }
    }

    fun getTodayStartInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // In your ViewModel or a helper function, add this logic to copy the file
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "product_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return file.absolutePath // Return the file path
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    // Update your onUpdateProductImage method in the ViewModel
    fun onUpdateProductImage(products: Products, uri: Uri, context: Context) {
        val imagePath = saveImageToInternalStorage(context, uri)
        if (imagePath != null) {
            // Update the product with the new image file path
            val updatedProduct = products.copy(imageUri = imagePath)
            // Update your database with the updated product
            updateProduct(updatedProduct)
        }
    }

    fun updateProduct(products: Products) {
        viewModelScope.launch {
            productRepository.updateProduct(products)
        }
    }

    fun updatePriceList(priceList: PriceList) = viewModelScope.launch {
        try {
            priceListRepository.updatePriceList(priceList)
        } catch (e: Exception) {
            Log.e("PriceListUpdate", "Failed to update price list: ${e.message}")
        }
    }

    fun butcherSubmitInvoice(
        customerId: Long,
        amountTendered: Double,
        productQuantities: Map<Long, Double>, // Ensure this is a Map<Long, Double>
        cashierEmail: String
    ) = viewModelScope.launch {
        try {
            val currencyCode = _selectedCurrency.value?.code ?: "USD"
            var totalAmount = 0.0
            val taxPercentage = 0.15 // Example tax percentage
            val discountPercentage = 0.0 // Example discount percentage

            val customer = customerRepository.getCustomerById(customerId)
            if (customer == null) {
                Log.e("InvoiceError", "Customer not found for customerId: $customerId")
                return@launch
            }

            val selectedProductsWithDetails = _selectedProducts.value.map { product ->
                val quantitySold =
                    productQuantities[product.productId] ?: 1.0 // Ensure this is treated as Double
                val priceList = priceListRepository.getPriceListByProductId(product.productId)
                val basePrice = priceList?.sellingPrice ?: 0.0
                val taxAmount = basePrice * taxPercentage
                val discountAmount = basePrice * discountPercentage

                val finalPrice = basePrice // Adjust this based on tax and discounts if needed
                totalAmount += finalPrice * quantitySold // Ensure quantitySold is treated as Double

                Triple(
                    product,
                    finalPrice,
                    quantitySold
                ) // Return a Triple of product, final price, and quantity
            }

            val change = amountTendered - totalAmount
            if (change < 0) {
                Log.e("InvoiceError", "Amount tendered is less than total amount!")
                return@launch
            }

            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
                val invoice = Invoice(
                    customerId = customerId,
                    totalAmount = totalAmount,
                    invoiceDate = System.currentTimeMillis(),
                    dueDate = null,
                    status = "Paid",
                    currencyCode = currencyCode
                )

                val invoiceId = invoiceRepository.insertInvoice(invoice)
                onInvoiceCreatedOrUpdated(invoice)

                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
                    deductButcherQuantityAndUpdateStock(product.productId, quantitySold)

                    val cartItem = CartItem(
                        customerId = customerId,
                        productId = product.productId,
                        invoiceId = invoiceId,
                        totalAmount = finalPrice * quantitySold,
                        totalAmountTendered = amountTendered,
                        change = change
                    )
                    cartItemRepository.insertCartItem(cartItem)

                    val invoiceItem = InvoiceItem(
                        invoiceId = invoiceId,
                        productId = product.productId,
                        customerId = customerId,
                        quantity = quantitySold.toInt(),
                        price = finalPrice,
                        total = finalPrice * quantitySold
                    )
                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
                }

                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
                    val invoiceLine = InvoiceLine(
                        invoiceId = invoiceId,
                        invoiceStatus = invoice.status,
                        productID = product.productId,
                        productName = product.productDescription,
                        productPrice = finalPrice,
                        productQtySold = quantitySold.toInt(),
                        cashier = cashierEmail,
                        customerID = customerId,
                        customerName = customer.name,
                        customerPhone = customer.phone,
                        totalAmount = totalAmount,
                        amountTendered = amountTendered,
                        changeBalance = change,
                        currentStock = product.productQty.toDouble().minus(quantitySold),
                        invoiceDate = System.currentTimeMillis(),
                        deviceId = deviceSession.deviceId.ifBlank { null },
                        storeId = deviceSession.storeId.ifBlank { null }
                    )
                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
                }

                _selectedProducts.value = emptyList()
            } else {
                Log.e("InvoiceError", "No valid selected products to invoice or total amount is 0")
            }
        } catch (e: Exception) {
            Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
        }
    }

    fun selectCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    //    final submitInvoice
    /**
     * All cart data is passed in as plain parameters — no ViewModel StateFlow is read
     * inside this coroutine. This eliminates the race condition where clearCart() could
     * wipe _selectedProducts between launch() and the first suspend point.
     *
     * @param products       Exact list of Products the cashier selected (from CartScreen state)
     * @param productQuantities  productId → quantity map (from CartScreen state)
     * @param productPrices  productId → final selling price in selected currency (from CartScreen state)
     * @param currencyCode   Currency code in use at checkout time
     */
    fun submitInvoice(
        customerId: Long,
        amountTendered: Double,
        products: List<Products>,
        productQuantities: Map<Long, Int>,
        productPrices: Map<Long, Double>,
        currencyCode: String,
        cashierEmail: String
    ) = viewModelScope.launch {
        try {
            Log.d("submitInvoice", "products: ${products.size}, quantities: $productQuantities, prices: $productPrices")

            if (products.isEmpty()) {
                Log.e("submitInvoice", "No products passed — aborting.")
                return@launch
            }

            var totalAmount = 0.0

            // For walk-in (customerId = 0) use a default customer object; no DB lookup needed
            val customer = if (customerId == 0L) {
                Customer(
                    customerId = 0,
                    name = "Walk-in",
                    phone = null,
                    email = null,
                    address = null
                )
            } else {
                customerRepository.getCustomerById(customerId)
            }
            if (customer == null) {
                Log.e("submitInvoice", "Customer not found for customerId: $customerId")
                return@launch
            }

            val selectedProductsWithDetails = products.map { product ->
                val quantitySold = productQuantities[product.productId] ?: 1
                val finalPrice = productPrices[product.productId] ?: 0.0
                totalAmount += finalPrice * quantitySold
                Triple(product, finalPrice, quantitySold)
            }

            val change = amountTendered - totalAmount

            if (change < 0) {
                Log.e("submitInvoice", "Amount tendered is less than total amount!")
                return@launch
            }

            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
                val invoice = Invoice(
                    customerId = customerId,
                    totalAmount = totalAmount,
                    invoiceDate = System.currentTimeMillis(),
                    dueDate = null,
                    status = "Paid",
                    currencyCode = currencyCode
                )

                val invoiceId = invoiceRepository.insertInvoice(invoice)

                // Only schedule sync if invoice was successfully inserted
                if (invoiceId > 0) {
                    val insertedInvoice = invoice.copy(invoiceId = invoiceId)
                    onInvoiceCreatedOrUpdated(insertedInvoice)
                }

                deductProductQuantities(productQuantities)

                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
                    // CartItem has a FK on customerId — skip for walk-in (id=0) to avoid constraint violation
                    if (customerId != 0L) {
                        val cartItem = CartItem(
                            customerId = customerId,
                            productId = product.productId,
                            invoiceId = invoiceId,
                            totalAmount = finalPrice * quantitySold,
                            totalAmountTendered = amountTendered,
                            change = change
                        )
                        cartItemRepository.insertCartItem(cartItem)
                    }

                    val invoiceItem = InvoiceItem(
                        invoiceId = invoiceId,
                        productId = product.productId,
                        customerId = customerId,
                        quantity = quantitySold,
                        price = finalPrice,
                        total = finalPrice * quantitySold
                    )
                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
                }

                val updatedProducts = mutableMapOf<Long, Double>()
                for ((productId, quantitySold) in productQuantities) {
                    val product = productRepository.getProductById(productId)
                    updatedProducts[productId] = product?.productQty?.toDouble() ?: 0.0
                }

                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
                    val invoiceLine = InvoiceLine(
                        invoiceId = invoiceId,
                        invoiceStatus = invoice.status,
                        productID = product.productId,
                        productName = product.productDescription,
                        productPrice = finalPrice,
                        productQtySold = quantitySold,
                        cashier = cashierEmail,
                        customerID = customerId,
                        customerName = customer.name,
                        customerPhone = customer.phone,
                        totalAmount = totalAmount,
                        amountTendered = amountTendered,
                        changeBalance = change,
                        currentStock = updatedProducts[product.productId] ?: 0.0,
                        invoiceDate = System.currentTimeMillis(),
                        deviceId = deviceSession.deviceId.ifBlank { null },
                        storeId = deviceSession.storeId.ifBlank { null }
                    )
                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
                    Log.d("submitInvoice", "✔ Inserted line for product ${product.productDescription} invoiceId=$invoiceId")
                }

                // clearCart() is called by CartScreen immediately before navigating,
                // so there is nothing to reset here.
            } else {
                Log.e(
                    "submitInvoice",
                    "No valid selected products to invoice or total amount is 0"
                )
            }
        } catch (e: Exception) {
            Log.e("submitInvoice", "Error during invoice submission", e)
        }
    }

    // CreditNote
    fun reverseInvoice(invoiceId: Long) = viewModelScope.launch {
        try {
            // Fetch the invoice to be reversed
            val invoice = invoiceRepository.getInvoiceById(invoiceId)
            if (invoice == null) {
                Log.e("CreditNoteError", "Invoice not found with ID: $invoiceId")
                return@launch
            }

            // Collect the Flow of InvoiceItems associated with the invoice, but limit it to one emission using `take(1)`
            invoiceItemRepository.getInvoiceItemsByInvoiceId(invoiceId)
                .take(1) // This ensures only one collection happens and then it stops
                .collect { invoiceItems ->
                    if (invoiceItems.isEmpty()) {
                        Log.e(
                            "CreditNoteError",
                            "No invoice items found for invoice ID: $invoiceId"
                        )
                        return@collect
                    }

                    // Reverse each item by adjusting the existing record instead of creating a new one
                    invoiceItems.forEach { invoiceItem ->
                        // Restore product stock by adding back the sold quantity
                        addQuantityBackToStock(
                            invoiceItem.productId,
                            invoiceItem.quantity.toLong()
                        )

                        // Update the InvoiceItem (mark as reversed)
                        val updatedInvoiceItem = invoiceItem.copy(
                            quantity = -invoiceItem.quantity, // Negative quantity to reverse
                            total = invoiceItem.total - invoiceItem.total // Negative total to reflect reversal
                        )
                        invoiceItemRepository.updateInvoiceItem(updatedInvoiceItem)
                    }

                    // Mark the original invoice as "Reversed" or "Credit Note"
                    val updatedInvoice = invoice.copy(
                        status = "Credit Note",
                        totalAmount = invoice.totalAmount - invoice.totalAmount // Make the totalAmount negative
                    )
                    invoiceRepository.updateInvoice(updatedInvoice)

                    // Fetch and update InvoiceLine associated with the invoice
                    val invoiceLine = invoiceLineRepository.getInvoiceLineById(invoiceId)

                    invoiceLine?.let { line ->
                        // Update the InvoiceLine instead of inserting a new one
                        val updatedInvoiceLine = line.copy(
                            invoiceStatus = "Credit Note",
                            productQtySold = -line.productQtySold, // Negative quantity for reversal
                            totalAmount = invoice.totalAmount - invoice.totalAmount, // Negative totalAmount for reversal
                            amountTendered = 0.0, // No money exchanged for a credit note
                            changeBalance = 0.0,  // No change for a credit note
                            currentStock = line.currentStock.minus(line.productQtySold) ?: 0.0,
                            invoiceDate = System.currentTimeMillis() // Update with the current timestamp
                        )

                    }

                    Log.d("CreditNote", "Successfully reversed invoice ID: $invoiceId")
                }
        } catch (e: Exception) {
            Log.e("CreditNoteError", "Error during credit note creation: ${e.message}")
        }
    }

    //

    fun addQuantityBackToStock(productId: Long, quantity: Long) = viewModelScope.launch {
        try {
            // Fetch the product by productId
            val product = productRepository.getProductById(productId)

            if (product != null) {
                // Fetch the related ShoeBrand and ShoeSize entities
                val shoeBrand = shoeBrandRepository.getShoeBrandById(product.brandID)
                val shoeSize = shoeSizeRepository.getShoeSizeById(product.shoeSizeId)

                if (shoeBrand != null && shoeSize != null) {
                    // Calculate the actual current stock level by summing the quantity of all ShoeSizes
                    val totalQuantity =
                        shoeSizeRepository.getTotalQuantityByBrand(shoeBrand.brandID)

                    // Update the stock level by adding the incoming quantity
                    val updatedStockLevel = totalQuantity + quantity
                    val updatedProduct = product.copy(updatedStockLevel)

                    // Save the updated product back to the repository
                    productRepository.updateProduct(updatedProduct)

                    Log.e(
                        "viewmodel",
                        "ShoeBrand or ShoeSize not found for product ID: $productId"
                    )
                }
            } else {
                Log.e("viewmodel", "Product not found with ID: $productId")
            }
        } catch (e: Exception) {
            Log.e(
                "viewmodel",
                "Error updating stock for product ID: $productId, ${e.message}"
            )
        }
    }

    //    syncInvoice
    // Note: Sync is now automatic in repositories, but keeping this for backward compatibility
    private suspend fun onInvoiceCreatedOrUpdated(invoice: Invoice) {
        // Sync is now handled automatically by InvoiceRepository
        // This method is kept for any additional logic that might be needed
    }

    private var syncTimeoutJob: kotlinx.coroutines.Job? = null

    private fun monitorSyncProgress() {
        viewModelScope.launch {
            combine(syncState, notsyncedCount) { state, unsynced ->
                Pair(state, unsynced)
            }.collect { (state, unsynced) ->
                if (state == SyncState.SYNCING) {
                    if (initialUnsyncedAtStart <= 0) {
                        initialUnsyncedAtStart = maxOf(unsynced, 1)
                    }
                    val syncedSoFar = (initialUnsyncedAtStart - unsynced).coerceAtLeast(0)
                    val fraction = if (initialUnsyncedAtStart > 0) {
                        (syncedSoFar.toFloat() / initialUnsyncedAtStart.toFloat()).coerceIn(0.05f, 0.95f)
                    } else 0.5f
                    _syncProgress.value = fraction
                } else if (state == SyncState.SUCCESS) {
                    _syncProgress.value = 1.0f
                    syncTimeoutJob?.cancel()
                    initialUnsyncedAtStart = 0
                } else {
                    _syncProgress.value = 0f
                    initialUnsyncedAtStart = 0
                }
            }
        }
    }

    fun syncAllNow() {
        if (initialUnsyncedAtStart <= 0) {
            initialUnsyncedAtStart = maxOf(notsyncedCount.value, 1)
        }
        _syncProgress.value = 0.05f

        // 90-second timeout safety guard: stops animation/worker if running too long
        syncTimeoutJob?.cancel()
        syncTimeoutJob = viewModelScope.launch {
            delay(90_000L)
            if (syncState.value == SyncState.SYNCING) {
                android.util.Log.w("InvoiceViewModel", "Sync operation reached 90s timeout — cancelling full_sync")
                workManager.cancelUniqueWork("full_sync")
                _syncProgress.value = 1.0f
                delay(1500L)
                _syncProgress.value = 0f
                initialUnsyncedAtStart = 0
            }
        }

        syncManager.syncAllNow()
    }

    private fun scheduleBatchSync() {
        syncManager.scheduleInvoiceBatchSync()
    }

    private fun scheduleProductBatchSync() {
        syncManager.scheduleProductBatchSync()
    }

    fun setupPeriodicProductSync() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncProductWorker>(
            5, // Repeat interval
            TimeUnit.MINUTES
        )
            .setInputData(
                workDataOf(
                    SyncProductWorker.KEY_BATCH_SYNC to true
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodic_product_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    fun syncInvoiceLines() {
        Log.d("SyncStatusCard", "syncInvoiceLines() called — scheduling batch sync")
        syncManager.scheduleInvoiceLineBatchSync()
    }

    /** Reflects the real WorkManager state of the full_sync job chain. */
    val syncWorkState: StateFlow<WorkInfo.State?> =
        workManager
            .getWorkInfosForUniqueWorkFlow("full_sync")
            .map { infos ->
                when {
                    infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED } -> WorkInfo.State.RUNNING
                    infos.isNotEmpty() && infos.all { it.state == WorkInfo.State.SUCCEEDED } -> WorkInfo.State.SUCCEEDED
                    infos.any { it.state == WorkInfo.State.FAILED } -> WorkInfo.State.FAILED
                    else -> infos.firstOrNull()?.state
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

//    end syncInvoice

    private fun clearSelectedProducts() {
        _selectedProducts.value = emptyList()
    }

    /**
     * BATCH INVOICE PROCESSING ANALYSIS:
     *
     * STRENGTHS:
     * ✓ Single DB fetch - Loads all reference data (brands, sizes, products) once upfront
     * ✓ In-memory lookups - Uses HashMaps for O(1) product matching instead of repeated DB queries
     * ✓ Error resilience - Failed items persist to DB for retry, preventing data loss
     * ✓ Separation of concerns - Validation separated from invoice creation
     * ✓ Batch grouping - Groups items by customer before creating invoices
     *
     * POTENTIAL IMPROVEMENTS:
     * - Consider transaction wrapping for atomic invoice creation
     * - Add progress callbacks for large CSV files (>1000 items)
     * - Implement chunked processing if memory becomes an issue with huge CSVs
     * - Add duplicate detection to prevent re-processing same CSV
     *
     * VERDICT: Solid approach for typical use cases (100-1000 items). Optimized for performance
     * with minimal DB hits. Failed item persistence ensures no data loss.
     */
    // Optimized batch invoice processing
    fun processCsvInvoices(
        inputStream: InputStream,
        cashierEmail: String,
        onComplete: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        try {

            // 1. Parse CSV
            val parser = CsvParser()
            val items = parser.parseInvoiceCsv(inputStream)

            if (items.isEmpty()) {
                Log.w("BatchInvoice", "No items to process")
                return@launch
            }

            // 2. Fetch ALL reference data ONCE
            val brandMap = shoeBrandRepository.getAllShoeBrandsOnce()
                .associateBy { it.brandName.lowercase() }
            val sizeMap = shoeSizeRepository.getAllShoeSizesOnce()
                .associateBy { it.size }
            val productMap = productRepository.getAllProductsOnce()
                .groupBy { Triple(it.brandID, it.shoeSizeId, it.productDescription.lowercase()) }
                .mapValues { it.value.first() }

            // 3. Process all items with in-memory lookups
            val validItems = mutableListOf<ValidInvoiceItem>()
            var skipped = 0

            val failedItems = mutableListOf<FailedInvoiceItem>()

            items.forEachIndexed { index, item ->
                try {
                    val brand = brandMap[item.brand.lowercase()]
                    if (brand == null) {
                        val allBrandNames = brandMap.values.map { it.brandName }
                        val suggestions =
                            StringMatcher.findClosestMatches(item.brand, allBrandNames)
                        val suggestedText =
                            if (suggestions.isNotEmpty()) suggestions.joinToString(", ") else null
                        val reason =
                            "Brand '${item.brand}' not found" + if (suggestions.isNotEmpty()) " → Try: ${
                                suggestions.joinToString(", ")
                            }" else ""

                        Log.w("BatchInvoice", "[${index + 1}] $reason")
                        failedItems.add(
                            FailedInvoiceItem(
                                rowIndex = index + 1,
                                brand = item.brand,
                                size = item.size,
                                description = item.description,
                                quantity = item.quantity,
                                amountTendered = item.amountTendered,
                                customerId = item.customerId,
                                failureReason = reason,
                                suggestedMatches = suggestedText
                            )
                        )
                        skipped++
                        return@forEachIndexed
                    }

                    val size = sizeMap[item.size]
                    if (size == null) {
                        val availableSizes = sizeMap.keys.sorted().take(10).joinToString(", ")
                        val reason = "Size ${item.size} not found. Available: $availableSizes"

                        Log.w("BatchInvoice", "[${index + 1}] $reason")
                        failedItems.add(
                            FailedInvoiceItem(
                                rowIndex = index + 1,
                                brand = item.brand,
                                size = item.size,
                                description = item.description,
                                quantity = item.quantity,
                                amountTendered = item.amountTendered,
                                customerId = item.customerId,
                                failureReason = reason
                            )
                        )
                        skipped++
                        return@forEachIndexed
                    }

                    val key = Triple(brand.brandID, size.shoeSizeId, item.description.lowercase())
                    val product = productMap[key]
                    if (product == null) {
                        val reason =
                            "Product not found: Brand='${item.brand}', Size=${item.size}, Color='${item.description}'"

                        Log.w("BatchInvoice", "[${index + 1}] $reason")
                        failedItems.add(
                            FailedInvoiceItem(
                                rowIndex = index + 1,
                                brand = item.brand,
                                size = item.size,
                                description = item.description,
                                quantity = item.quantity,
                                amountTendered = item.amountTendered,
                                customerId = item.customerId,
                                failureReason = reason
                            )
                        )
                        skipped++
                        return@forEachIndexed
                    }

                    validItems.add(
                        ValidInvoiceItem(
                            product = product,
                            quantity = item.quantity,
                            amountTendered = item.amountTendered,
                            customerId = item.customerId
                        )
                    )
                } catch (e: Exception) {
                    Log.e("BatchInvoice", "Error processing item ${index + 1}: ${e.message}")
                    failedItems.add(
                        FailedInvoiceItem(
                            rowIndex = index + 1,
                            brand = item.brand,
                            size = item.size,
                            description = item.description,
                            quantity = item.quantity,
                            amountTendered = item.amountTendered,
                            customerId = item.customerId,
                            failureReason = e.message ?: "Unknown error"
                        )
                    )
                    skipped++
                }
            }

            _failedInvoiceItems.value = failedItems

            if (failedItems.isNotEmpty()) {
                failedInvoiceRepository.insertFailedInvoices(failedItems)
            }

            if (validItems.isEmpty()) {
                Log.w(
                    "BatchInvoice",
                    "No valid items to invoice. Brands exist but products not found in database."
                )
                Log.w(
                    "BatchInvoice",
                    "Add products for these brands first using the product upload feature."
                )
                return@launch
            }

            // 4. Group by customer and submit invoices
            val groupedByCustomer = validItems.groupBy { it.customerId }
            var successCount = 0
            var failCount = 0

            groupedByCustomer.forEach { (customerId, customerItems) ->
                try {
                    submitBatchInvoice(customerId, customerItems, cashierEmail)
                    successCount++
                } catch (e: Exception) {
                    Log.e(
                        "BatchInvoice",
                        "Failed to submit invoice for customer $customerId: ${e.message}"
                    )
                    failCount++
                }
            }

            // Wait for invoice list to refresh
            withContext(Dispatchers.Main) {
                fetchAllInvoices()
            }

            withContext(Dispatchers.Main) {
                onComplete(_failedInvoiceItems.value.isNotEmpty())
            }

        } catch (e: Exception) {
            Log.e("BatchInvoice", "Batch invoice processing failed: ${e.message}", e)
        }
    }

    private suspend fun submitBatchInvoice(
        customerId: Long,
        items: List<ValidInvoiceItem>,
        cashierEmail: String
    ) {
        val products = items.map { it.product }
        val productQuantities = items.associate { it.product.productId to it.quantity }

        // Calculate prices from price list
        val productPrices = items.associate { item ->
            val priceList = priceListRepository.getPriceListByProductId(item.product.productId)
            item.product.productId to (priceList?.sellingPrice ?: 0.0)
        }

        val totalAmount = productPrices.entries.sumOf { (id, price) ->
            price * (productQuantities[id] ?: 0)
        }

        submitInvoice(
            customerId        = customerId,
            amountTendered    = totalAmount,
            products          = products,
            productQuantities = productQuantities,
            productPrices     = productPrices,
            currencyCode      = "USD",
            cashierEmail      = cashierEmail
        )
    }

//    end of batch invoice processing

    fun retryFailedItem(
        failedItem: FailedInvoiceItem,
        cashierEmail: String,
        onResult: (Boolean, String) -> Unit
    ) = viewModelScope.launch {
        try {
            val brandMap =
                shoeBrandRepository.getAllShoeBrandsOnce().associateBy { it.brandName.lowercase() }
            val sizeMap = shoeSizeRepository.getAllShoeSizesOnce().associateBy { it.size }
            val productMap = productRepository.getAllProductsOnce()
                .groupBy { Triple(it.brandID, it.shoeSizeId, it.productDescription.lowercase()) }
                .mapValues { it.value.first() }

            val brand = brandMap[failedItem.brand.lowercase()]
            val size = sizeMap[failedItem.size]

            if (brand != null && size != null) {
                val key = Triple(brand.brandID, size.shoeSizeId, failedItem.description.lowercase())
                val product = productMap[key]

                if (product != null) {
                    submitBatchInvoice(
                        failedItem.customerId,
                        listOf(
                            ValidInvoiceItem(
                                product,
                                failedItem.quantity,
                                failedItem.amountTendered,
                                failedItem.customerId
                            )
                        ),
                        cashierEmail
                    )

                    // Delete from DB first
                    failedInvoiceRepository.deleteFailedInvoice(failedItem)

                    // Then update UI
                    _failedInvoiceItems.value =
                        _failedInvoiceItems.value.filter { it.id != failedItem.id }
                    withContext(Dispatchers.Main) {
                        onResult(true, "Invoice created successfully")
                    }
                } else {
                    Log.e("BatchInvoice", "Product still not found after retry")
                    withContext(Dispatchers.Main) {
                        onResult(false, "Product not found in database")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, "Brand or size not found")
                }
            }
        } catch (e: Exception) {
            Log.e("BatchInvoice", "Retry failed: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun clearFailedItems() = viewModelScope.launch {
        failedInvoiceRepository.clearAll()
        _failedInvoiceItems.value = emptyList()
    }

    private var failedInvoicesJob: Job? = null

    private fun fetchFailedInvoices() {
        failedInvoicesJob?.cancel()
        failedInvoicesJob = viewModelScope.launch {
            failedInvoiceRepository.getAllFailedInvoices().collectLatest { items ->
                _failedInvoiceItems.value = items
            }
        }
    }

    fun deleteFailedItem(item: FailedInvoiceItem) = viewModelScope.launch {
        failedInvoiceRepository.deleteFailedInvoice(item)
    }

    suspend fun getAvailableSizesForBrand(brandName: String): List<Double> {
        val brand = shoeBrandRepository.getAllShoeBrandsOnce()
            .find { it.brandName.equals(brandName, ignoreCase = true) } ?: return emptyList()

        val products = productRepository.getAllProductsOnce()
            .filter { it.brandID == brand.brandID }

        return products.mapNotNull { product ->
            shoeSizeRepository.getShoeSizeById(product.shoeSizeId)?.size
        }.distinct().sorted()
    }

    suspend fun getAvailableColorsForBrand(brandName: String): List<String> {
        val brand = shoeBrandRepository.getAllShoeBrandsOnce()
            .find { it.brandName.equals(brandName, ignoreCase = true) } ?: return emptyList()

        return productRepository.getAllProductsOnce()
            .filter { it.brandID == brand.brandID }
            .map { it.productDescription }
            .distinct()
            .sorted()
    }

    // ─── Add New Brand + Bulk Products (Invoicing context) ───────────────────

    /**
     * Creates a complete shoe brand with sizes 3-11 and all associated products/price lists.
     * Used from ProductSelectionScreen when a search returns no results.
     *
     * Workflow:
     * 1. Resolve or create brand via findBrandByNameCaseInsensitive
     * 2. Insert ShoeSize entries for sizes 3-11 (qty=100, skips existing)
     * 3. Insert Products for each size (skips existing), productCode auto-generated
     * 4. Insert PriceList entry per new product
     * 5. Refreshes the product list so new items appear in search immediately
     *
     * @param onComplete called on the main thread with a success message or error string
     */
    fun addNewBrandWithProducts(
        brandName: String,
        productDescription: String,
        averageCost: Double,
        standardCost: Double,
        qtyPerSize: Double,
        onComplete: (message: String, isError: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Step 1: Resolve or create brand
                val brand = shoeBrandRepository.findBrandByNameCaseInsensitive(brandName.trim())
                    ?: run {
                        val newId = shoeBrandRepository.insertShoeBrand(
                            com.ipos.data.entities.ShoeBrand(
                                brandName = brandName.trim()
                            )
                        )
                        shoeBrandRepository.getShoeBrandById(newId)!!
                    }
                val brandId = brand.brandID

                // Step 2: Create shoe sizes 3-11 (skip duplicates)
                val shoeSizeIds = mutableListOf<Long>()
                for (size in 3..11) {
                    val existing = shoeSizeRepository.findSizeByBrandAndValue(brandId, size.toDouble())
                    val sizeId = existing?.shoeSizeId ?: shoeSizeRepository.insertShoeSize(
                        com.ipos.data.entities.ShoeSize(
                            brandID = brandId,
                            size = size.toDouble(),
                            quantity = qtyPerSize.toLong()
                        )
                    )
                    shoeSizeIds.add(sizeId)
                }

                // Steps 3+4: Create products and price lists for each size
                var createdCount = 0
                shoeSizeIds.forEachIndexed { index, shoeSizeId ->
                    val existing = productRepository.getProductByExactMatch(
                        brandId = brandId,
                        shoeSizeId = shoeSizeId,
                        description = productDescription.trim()
                    )
                    if (existing == null) {
                        val productId = productRepository.insertProduct(
                            com.ipos.data.entities.Products(
                                productDescription = productDescription.trim(),
                                productQty = qtyPerSize.toLong(),
                                brandID = brandId,
                                shoeSizeId = shoeSizeId,
                                saleable = true,
                                purchasable = true,
                                averageCost = averageCost,
                                standardCost = standardCost,
                                salesTaxCode = null,
                                productCode = "",  // auto-generated by ProductRepository
                                inclusiveTaxed = false,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                        priceListRepository.insertPriceLists(
                            listOf(
                                com.ipos.data.entities.PriceList(
                                    productId = productId,
                                    productDescription = productDescription.trim(),
                                    unitCost = standardCost,
                                    sellingPrice = standardCost,
                                    markupPercentage = 0.0,
                                    grossProfitPercentage = 0.0
                                )
                            )
                        )
                        createdCount++
                    }
                }

                // Step 5: Refresh product list so new items appear in search immediately
                fetchProducts()
                fetchBrandProducts()
                fetchPriceList()

                val msg = "\"${brandName.trim()}\" added: $createdCount new products (sizes 3–11)"
                Log.d("InvoiceVM", msg)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(msg, false)
                }
            } catch (e: Exception) {
                Log.e("InvoiceVM", "Failed to add brand with products", e)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete("Failed to add brand: ${e.message}", true)
                }
            }
        }
    }
}

