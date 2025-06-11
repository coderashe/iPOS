package com.example.learncompose303.viewmodels

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
import com.example.learncompose303.R
import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.data.domain.ProductWithBrand
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.CsvParser
import com.example.learncompose303.data.entities.Currency
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.data.entities.User
import com.example.learncompose303.data.repositories.AuthRepository
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.DayReportsRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepository
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import com.example.learncompose303.data.repositories.InvoiceRepository
import com.example.learncompose303.data.repositories.OrderRepository
import com.example.learncompose303.data.repositories.PriceListRepository
import com.example.learncompose303.data.repositories.ProductDayReportRepository
import com.example.learncompose303.data.repositories.ProductRepository
import com.example.learncompose303.data.repositories.ShoeBrandRepository
import com.example.learncompose303.data.repositories.ShoeSizeRepository
import com.example.learncompose303.data.repositories.StockTakeRepository
import com.example.learncompose303.data.repositories.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
import com.example.learncompose303.data.CsvInvoiceItem
import com.example.learncompose303.network.SyncInvoiceWorker
import com.example.learncompose303.network.SyncProductWorker
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
    private val studentRepository: StudentRepository,
    private val shoeSizeRepository: ShoeSizeRepository,
    private val shoeBrandRepository: ShoeBrandRepository,
    private val authRepository: AuthRepository,
    private val dayReportRepository: DayReportsRepository,
    private val productDayReportRepository: ProductDayReportRepository,
    private val stockTakeRepository: StockTakeRepository,
    private val workManager: WorkManager
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

    private val _invoiceLines = MutableStateFlow<List<InvoiceLine>>(emptyList())
    val invoiceLines: StateFlow<List<InvoiceLine>> get() = _invoiceLines

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    val currencies: StateFlow<List<Currency>> = _currencies

    private val _selectedCurrency = MutableStateFlow<Currency?>(null)
    val selectedCurrency: StateFlow<Currency?> = _selectedCurrency

    private val _withBrandProductList = MutableStateFlow(emptyList<ProductWithBrand>())
    val withBrandProductList = _withBrandProductList.asStateFlow()

    // State for unsynced count
    private val _unsyncedCount = MutableStateFlow(0)
    val notsyncedCount: StateFlow<Int> = _unsyncedCount.asStateFlow()
    private var unsyncedCount = 0

    // Track sync status
    private val _syncStatus = MutableLiveData<String>("Idle")
    val syncStatus: LiveData<String> = _syncStatus

    companion object {
        private const val TAG = "InvoiceViewModel"
        private const val SYNC_BATCH_SIZE = 50
        private const val BATCH_SYNC_WORK_NAME = "invoice_batch_sync"
        const val PRODUCT_BATCH_SYNC_WORK_NAME = "product_batch_sync_work"
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
        // Now insert initial products
    }

    private fun monitorUnsyncedCount() {
        viewModelScope.launch {
            invoiceRepository.getUnsyncedCount().collectLatest { count ->
                _unsyncedCount.value = count
            }
        }
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
            Log.d("viewmodel", "Updated Shoe size list: $shoeSizesList")
        }
    }

    // Fetch all shoe brands
    fun fetchShoeBrands() = viewModelScope.launch {
        shoeBrandRepository.getAllShoeBrands().collectLatest { shoeBrandsList ->
            _shoeBrands.value = shoeBrandsList
            Log.d("viewmodel", "Updated Shoe brand list: $shoeBrandsList")
        }
    }

    // Logic for uploading products from CSV
    fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
        viewModelScope.launch {
            try {
                Log.d("ProductUpload", "Starting CSV upload process")

                val parser = CsvParser()
                val productsFromCsv = parser.parseProductCsv(inputStream)
                Log.d("ProductUpload", "Parsed ${productsFromCsv.size} products from CSV")

                val existingProducts = productRepository.getAllProductsOnce()
                Log.d("ProductUpload", "Fetched ${existingProducts.size} existing products")

                var updatedCount = 0
                var insertedCount = 0
                var errorCount = 0

                // Create a clean key map for existing products
                val existingProductMap = existingProducts.associateBy {
                    Triple(
                        it.productDescription.trim().lowercase(),
                        it.shoeSizeId ?: -1L,
                        it.brandID ?: -1L
                    )
                }

                for ((index, productCsv) in productsFromCsv.withIndex()) {
                    try {
                        Log.d(
                            "ProductUpload",
                            "Processing product ${index + 1}/${productsFromCsv.size}: ${productCsv.productDescription}"
                        )

                        val description = productCsv.productDescription.trim()
                        val shoeSizeId = productCsv.shoeSizeId ?: -1L
                        val brandID = productCsv.brandID ?: -1L

                        val key = Triple(description.lowercase(), shoeSizeId, brandID)
                        val existingProduct = existingProductMap[key]

                        val uniqueProductCode = if (productCsv.productCode.contains("H&P")) {
                            "${productCsv.productCode}_${description}_$shoeSizeId"
                        } else {
                            productCsv.productCode
                        }

                        if (existingProduct != null) {
                            Log.d("ProductUpload", "Product exists - updating: $description")

                            val updatedProduct = existingProduct.copy(
                                productQty = productCsv.productQty,
                                brandID = brandID,
                                shoeSizeId = shoeSizeId,
                                saleable = productCsv.saleable,
                                purchasable = productCsv.purchasable,
                                averageCost = productCsv.averageCost,
                                standardCost = productCsv.standardCost
                                    ?: existingProduct.standardCost,
                                salesTaxCode = productCsv.salesTaxCode,
                                productCode = uniqueProductCode,
                                inclusiveTaxed = productCsv.inclusiveTaxed,
                                imageUri = productCsv.imageUri ?: existingProduct.imageUri,
                                lastUpdated = System.currentTimeMillis()
                            )

                            productRepository.updateProduct(updatedProduct)
                            updatedCount++

                            updatePriceListForProduct(updatedProduct)
                        } else {
                            Log.d(
                                "ProductUpload",
                                "Product does not exist - inserting new: $description"
                            )

                            val imageUri = productCsv.imageUri ?: getDefaultImageUri(context)
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

                            val productId = productRepository.insertProduct(newProduct)
                            insertedCount++

                            createPriceListForProduct(productId, newProduct)
                        }
                    } catch (e: Exception) {
                        errorCount++
                        Log.e(
                            "ProductUpload",
                            "Error processing product: ${productCsv.productDescription}",
                            e
                        )
                    }
                }

                Log.d(
                    "ProductUpload",
                    "Upload completed - $insertedCount inserted, $updatedCount updated, $errorCount errors"
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Products uploaded: $insertedCount new, $updatedCount updated, $errorCount errors",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("ProductUpload", "Failed to upload products", e)
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

//    fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
//        viewModelScope.launch {
//            try {
//                Log.d("ProductUpload", "Starting CSV upload process")
//
//                val parser = CsvParser()
//                val productsFromCsv = parser.parseProductCsv(inputStream)
//                Log.d("ProductUpload", "Parsed ${productsFromCsv.size} products from CSV")
//
//                val existingProducts = productRepository.getAllProductsOnce()
//                Log.d(
//                    "ProductUpload",
//                    "Fetched ${existingProducts.size} existing products from database"
//                )
//
//                var updatedCount = 0
//                var insertedCount = 0
//                var errorCount = 0
//
//                val existingProductMap = existingProducts.associateBy {
//                    Triple(it.productDescription, it.shoeSizeId, it.brandID)
//                }
//
//                for ((index, productCsv) in productsFromCsv.withIndex()) {
//                    try {
//                        Log.d(
//                            "ProductUpload",
//                            "Processing product ${index + 1}/${productsFromCsv.size}: ${productCsv.productDescription}"
//                        )
//
//                        val uniqueProductCode = if (productCsv.productCode.contains("H&P")) {
//                            "${productCsv.productCode}_${productCsv.productDescription}_${productCsv.shoeSizeId}"
//                        } else {
//                            productCsv.productCode
//                        }
//
//
//                        val key = Triple(
//                            productCsv.productDescription,
//                            productCsv.shoeSizeId,
//                            productCsv.brandID
//                        )
//
//                        val existingProduct = existingProductMap[key]
//
//                        if (existingProduct != null) {
//                            Log.d(
//                                "ProductUpload",
//                                "Product exists - updating: ${productCsv.productDescription}"
//                            )
//
//                            val updatedProduct = existingProduct.copy(
//                                productQty = productCsv.productQty,
//                                brandID = productCsv.brandID,
//                                shoeSizeId = productCsv.shoeSizeId,
//                                saleable = productCsv.saleable,
//                                purchasable = productCsv.purchasable,
//                                averageCost = productCsv.averageCost,
//                                standardCost = productCsv.standardCost
//                                    ?: existingProduct.standardCost,
//                                salesTaxCode = productCsv.salesTaxCode,
//                                productCode = uniqueProductCode,
//                                inclusiveTaxed = productCsv.inclusiveTaxed,
//                                imageUri = productCsv.imageUri ?: existingProduct.imageUri,
//                                lastUpdated = System.currentTimeMillis()
//                            )
//
//                            productRepository.updateProduct(updatedProduct)
//                            Log.d(
//                                "ProductUpload",
//                                "Updated product: ${updatedProduct.productDescription}"
//                            )
//                            updatedCount++
//
//                            updatePriceListForProduct(updatedProduct)
//                            Log.d(
//                                "ProductUpload",
//                                "Updated price list for: ${updatedProduct.productDescription}"
//                            )
//                        } else {
//                            Log.d(
//                                "ProductUpload",
//                                "Product does not exist - inserting new product: ${productCsv.productDescription}"
//                            )
//
//                            val imageUri = productCsv.imageUri ?: getDefaultImageUri(context)
//                            val standardCost = productCsv.standardCost ?: 0.0
//
//                            val newProduct = Products(
//                                productDescription = productCsv.productDescription,
//                                productQty = productCsv.productQty,
//                                brandID = productCsv.brandID,
//                                shoeSizeId = productCsv.shoeSizeId,
//                                saleable = productCsv.saleable,
//                                purchasable = productCsv.purchasable,
//                                averageCost = productCsv.averageCost,
//                                standardCost = standardCost,
//                                salesTaxCode = productCsv.salesTaxCode,
//                                productCode = uniqueProductCode,
//                                inclusiveTaxed = productCsv.inclusiveTaxed,
//                                imageUri = imageUri,
//                                lastUpdated = System.currentTimeMillis()
//                            )
//
//                            val productId = productRepository.insertProduct(newProduct)
//                            Log.d(
//                                "ProductUpload",
//                                "Inserted product: ${newProduct.productDescription} with ID: $productId"
//                            )
//                            insertedCount++
//
//                            createPriceListForProduct(productId, newProduct)
//                            Log.d(
//                                "ProductUpload",
//                                "Created price list for new product: ${newProduct.productDescription}"
//                            )
//                        }
//                    } catch (e: Exception) {
//                        errorCount++
//                        Log.e(
//                            "ProductUpload",
//                            "Error processing product: ${productCsv.productDescription}",
//                            e
//                        )
//                    }
//                }
//
//                Log.d(
//                    "ProductUpload",
//                    "Upload completed - $insertedCount inserted, $updatedCount updated, $errorCount errors"
//                )
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        context,
//                        "Products uploaded: $insertedCount new, $updatedCount updated, $errorCount errors",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            } catch (e: Exception) {
//                Log.e("ProductUpload", "Failed to upload products", e)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        context,
//                        "Failed to upload products: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }

//    fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
//        viewModelScope.launch {
//            try {
//                val parser = CsvParser()
//                val productsFromCsv = parser.parseProductCsv(inputStream)
//
//                // Retrieve existing products from the database using the once method
//                val existingProducts = productRepository.getAllProductsOnce()
//
//                // Track stats for user feedback
//                var updatedCount = 0
//                var insertedCount = 0
//                var errorCount = 0
//
//                // Create a map to track existing products by their description and shoe size
//                val existingProductMap = existingProducts.associateBy {
//                    Triple(it.productDescription, it.shoeSizeId, it.brandID)
//                }
//
//                for (productCsv in productsFromCsv) {
//                    try {
//                        // Generate a unique product code if needed
//                        val uniqueProductCode = if (productCsv.productCode.contains("E+")) {
//                            // If the code is in scientific notation (like 2.13E+09), make it unique
//                            "${productCsv.productCode}_${productCsv.productDescription}_${productCsv.shoeSizeId}"
//                        } else {
//                            productCsv.productCode
//                        }
//
//                        val key = Triple(
//                            productCsv.productDescription,
//                            productCsv.shoeSizeId,
//                            productCsv.brandID
//                        )
//                        val existingProduct = existingProductMap[key]
//
//                        if (existingProduct != null) {
//                            // If the product exists, update its details
//                            val updatedProduct = existingProduct.copy(
//                                productQty = productCsv.productQty,
//                                brandID = productCsv.brandID,
//                                shoeSizeId = productCsv.shoeSizeId,
//                                saleable = productCsv.saleable,
//                                purchasable = productCsv.purchasable,
//                                averageCost = productCsv.averageCost,
//                                standardCost = productCsv.standardCost
//                                    ?: existingProduct.standardCost,
//                                salesTaxCode = productCsv.salesTaxCode,
//                                productCode = uniqueProductCode, // Use the unique code
//                                inclusiveTaxed = productCsv.inclusiveTaxed,
//                                imageUri = productCsv.imageUri ?: existingProduct.imageUri,
//                                lastUpdated = System.currentTimeMillis()
//                            )
//
//                            // Update the product in the repository
//                            productRepository.updateProduct(updatedProduct)
//                            updatedCount++
//
//                            // Update the price list for the existing product
//                            updatePriceListForProduct(updatedProduct)
//                        } else {
//                            // Product does not exist, add it
//                            val imageUri = productCsv.imageUri ?: getDefaultImageUri(context)
//                            val standardCost = productCsv.standardCost ?: 0.0
//
//                            // Create a new product entity
//                            val newProduct = Products(
//                                productDescription = productCsv.productDescription,
//                                productQty = productCsv.productQty,
//                                brandID = productCsv.brandID,
//                                shoeSizeId = productCsv.shoeSizeId,
//                                saleable = productCsv.saleable,
//                                purchasable = productCsv.purchasable,
//                                averageCost = productCsv.averageCost,
//                                standardCost = standardCost,
//                                salesTaxCode = productCsv.salesTaxCode,
//                                productCode = uniqueProductCode, // Use the unique code
//                                inclusiveTaxed = productCsv.inclusiveTaxed,
//                                imageUri = imageUri,
//                                lastUpdated = System.currentTimeMillis()
//                            )
//
//                            Log.d(
//                                "ProductUpload",
//                                "Attempting insert: ${newProduct.productDescription}, " +
//                                        "brandID: ${newProduct.brandID}, shoeSizeId: ${newProduct.shoeSizeId}"
//                            )
//
//                            // Insert new product into the repository
//                            val productId = productRepository.insertProduct(newProduct)
//                            insertedCount++
//
//                            // Insert price list for the new product
//                            createPriceListForProduct(productId, newProduct)
//
//                            Log.d(
//                                "ProductUpload",
//                                "Inserted product: ${newProduct.productDescription}, Size: ${newProduct.shoeSizeId}"
//                            )
//                        }
//                    } catch (e: Exception) {
//                        errorCount++
//                        Log.e(
//                            "ProductUpload",
//                            "Error processing product: ${productCsv.productDescription}",
//                            e
//                        )
//                    }
//                }
//
//                // Log summary statistics
//                Log.d(
//                    "ProductUpload",
//                    "Upload complete: $insertedCount inserted, $updatedCount updated, $errorCount errors"
//                )
//
//                // Show toast notification with detailed results
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        context,
//                        "Products uploaded: $insertedCount new, $updatedCount updated, $errorCount errors",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            } catch (e: Exception) {
//                Log.e("ProductUpload", "Failed to upload products", e)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        context,
//                        "Failed to upload products: ${e.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }

    // Helper function to update price list for existing products
    private fun updatePriceListForProduct(product: Products) {
        val sellingPrice = product.standardCost * 1 // Adjust markup factor as needed
        val markupPercentage = ((sellingPrice - product.standardCost) / product.standardCost) * 100
        val grossProfitPercentage = ((sellingPrice - product.averageCost) / sellingPrice) * 100

        val priceList = PriceList(
            productId = product.productId,
            productDescription = product.productDescription,
            unitCost = product.standardCost,
            sellingPrice = sellingPrice,
            markupPercentage = markupPercentage,
            grossProfitPercentage = grossProfitPercentage
        )
        updatePriceList(priceList)
    }

    // Helper function to create price list for new products
    private fun createPriceListForProduct(productId: Long, product: Products) {
        val sellingPrice = product.standardCost * 1 // Adjust markup factor as needed
        val markupPercentage = ((sellingPrice - product.standardCost) / product.standardCost) * 100
        val grossProfitPercentage = ((sellingPrice - product.averageCost) / sellingPrice) * 100

        val priceList = PriceList(
            productId = productId,
            productDescription = product.productDescription,
            unitCost = product.standardCost,
            sellingPrice = sellingPrice,
            markupPercentage = markupPercentage,
            grossProfitPercentage = grossProfitPercentage
        )

        insertPriceList(priceList)
    }

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
        Log.e("viewmodellogs", "Fetching invoice lines for invoiceId: $invoiceId")
        val invoiceLineList = invoiceLineRepository.getInvoiceLinesByInvoiceId(invoiceId)
        Log.e("viewmodellogs", "Fetched invoice lines: $invoiceLineList")
        _invoiceLines.value = invoiceLineList
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
            val initialUser = User(
                userFullName = "Simba Start",
                userContact = "0776815030",
                userRole = "admin",
                userPassword = "password",
                storeID = "istart01",
                userEmail = "dr.simbastart@gmail.com",
                userAddress = "3 Maywood St, Kwekwe, Zimbabwe"
            )
            authRepository.signUpUser(initialUser)
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

    // Insert initial shoe brands
    fun insertInitialShoeBrands() = viewModelScope.launch {
        val existingBrands = shoeBrandRepository.getAllShoeBrandsOnce()

        if (existingBrands.isEmpty()) {
            val initialBrands = listOf(
                ShoeBrand(1, "OLDSTOCK"),
                ShoeBrand(2, "D166"),
                ShoeBrand(3, "6610"),
                ShoeBrand(4, "SY24023"),
                ShoeBrand(5, "6886"),
                ShoeBrand(6, "D159"),
                ShoeBrand(7, "W2127A"),
                ShoeBrand(8, "W2127J"),
                ShoeBrand(9, "D160"),
                ShoeBrand(10, "Z7283"),
                ShoeBrand(11, "QY-247"),
                ShoeBrand(12, "QY-122"),
                ShoeBrand(13, "W23085B"),
                ShoeBrand(14, "N05-1"),
                ShoeBrand(15, "NF07-7"),
                ShoeBrand(16, "NF07-9"),
                ShoeBrand(17, "202588-3"),
                ShoeBrand(18, "T18"),
                ShoeBrand(19, "W24050D"),
                ShoeBrand(20, "RR17-5"),
                ShoeBrand(21, "RR17-6"),
                ShoeBrand(22, "RR17-1"),
                ShoeBrand(23, "RR17-7"),
                ShoeBrand(24, "NF25-2"),
                ShoeBrand(25, "RR17-4"),
                ShoeBrand(26, "6620"),
                ShoeBrand(27, "NF256"),
                ShoeBrand(28, "NF164"),
                ShoeBrand(29, "N04-3"),
                ShoeBrand(30, "N04-1"),
                ShoeBrand(31, "2893"),
                ShoeBrand(32, "6610-2"),
                ShoeBrand(33, "6610-4"),
                ShoeBrand(34, "005"),
                ShoeBrand(35, "NF24-1"),
                ShoeBrand(36, "NF24-2"),
                ShoeBrand(37, "NF24-5"),
                ShoeBrand(38, "NF24-4"),
                ShoeBrand(39, "NF24-3"),
                ShoeBrand(40, "M265"),
                ShoeBrand(41, "M256"),
                ShoeBrand(42, "831"),
                ShoeBrand(43, "836-2"),
                ShoeBrand(44, "827-2"),
                ShoeBrand(45, "NF29-1"),
                ShoeBrand(46, "NF29-2"),
                ShoeBrand(47, "NF29-3"),
                ShoeBrand(48, "206-3"),
                ShoeBrand(49, "W24145A"),
                ShoeBrand(50, "2088-1"),
                ShoeBrand(51, "N05-3"),
                ShoeBrand(52, "15010-9"),
                ShoeBrand(53, "206-4"),
                ShoeBrand(54, "913"),
                ShoeBrand(55, "M23085F"),
                ShoeBrand(56, "X-9956"),
                ShoeBrand(57, "NF51-1"),
                ShoeBrand(58, "NF51-2"),
                ShoeBrand(59, "NF51-3"),
                ShoeBrand(60, "NF51-4"),
                ShoeBrand(61, "NF51-5"),
                ShoeBrand(62, "NF50-1"),
                ShoeBrand(63, "NF50-2"),
                ShoeBrand(64, "NF50-3"),
                ShoeBrand(65, "NF50-4"),
                ShoeBrand(66, "NF50-5"),
                ShoeBrand(67, "NF50-6"),
                ShoeBrand(68, "NF06-1"),
                ShoeBrand(69, "NF06-2"),
                ShoeBrand(70, "NF06-4"),
                ShoeBrand(71, "NF06-7"),
                ShoeBrand(72, "830-5"),
                ShoeBrand(73, "02-6"),
                ShoeBrand(74, "09-8"),
                ShoeBrand(75, "W22103A")
            )
            shoeBrandRepository.insertShoeBrands(initialBrands)
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
        Log.d("InvoiceViewModel", "Today's Date: $today")

        // Log the entire order list for debugging
        Log.d("InvoiceViewModel", "Order List: ${_orderList.value}")

        // Filter orders for today
        val todayOrders = _orderList.value.filter { order ->
            // Assuming `order.orderDate` is in milliseconds
            val orderDate = LocalDate.ofEpochDay(order.orderDate / (24 * 60 * 60 * 1000))

            // Log each order date being checked
            Log.d(
                "InvoiceViewModel",
                "Order Date: ${order.orderDate}, LocalDate: $orderDate"
            )

            // Check if the order date matches today
            orderDate == today
        }

        // Get the number of today's orders
        val totalOrdersForToday = todayOrders.size

        // Log the filtered orders and total for debugging
        Log.d("InvoiceViewModel", "Today's Orders: $todayOrders")
        Log.d("InvoiceViewModel", "Total Orders for Today: $totalOrdersForToday")

        return totalOrdersForToday
    }


    // Function to compute today's sales total and number of transactions
    fun getTodaySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()

        // Log the date
        Log.d("InvoiceViewModel", "Today's Date: $today")

        // Filter invoices for today
        val todayInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate = LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))
            // Log each invoice date being checked
            Log.d("todays", "Invoice Date: $invoiceDate")
            invoiceDate.isEqual(today)
        }

        Log.d("todays", "DEBUG - getTodaySalesInfo found ${todayInvoices.size} invoices")

        // Group invoices by currency and sum totals
        val currencyTotals = todayInvoices.groupBy { it.currencyCode }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        // Count total number of transactions for today
        val transactionCount = todayInvoices.size

        // Log the filtered invoices for debugging
        Log.d("InvoiceViewModel", "Today's Invoices: $todayInvoices")
        Log.d(
            "InvoiceViewModel",
            "Currency Totals: $currencyTotals, Transaction Count: $transactionCount"
        )

        return currencyTotals to transactionCount
    }

    //    new
    fun getWeeklySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        // Log the week range
        Log.d("InvoiceViewModel", "Week Range: $startOfWeek to $endOfWeek")

        // Filter invoices for the current week
        val weeklyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate = LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))
            // Log each invoice date being checked
            Log.d("InvoiceViewModel", "Invoice Date: $invoiceDate")
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
        Log.d("InvoiceViewModel", "Weekly Invoices: $weeklyInvoices")
        Log.d(
            "InvoiceViewModel",
            "Currency Totals: $currencyTotals, Transaction Count: $transactionCount"
        )

        return currencyTotals to transactionCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlySalesInfo(): Pair<Map<String, Double>, Int> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        // Log the month range
        Log.d("InvoiceViewModel", "Month Range: $startOfMonth to $endOfMonth")

        // Filter invoices for the current month
        val monthlyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate = LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))
            // Log each invoice date being checked
            Log.d("InvoiceViewModel", "Invoice Date: $invoiceDate")
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
        Log.d("InvoiceViewModel", "Monthly Invoices: $monthlyInvoices")
        Log.d(
            "InvoiceViewModel",
            "Currency Totals: $currencyTotals, Transaction Count: $transactionCount"
        )

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

            Log.d(
                "StockUpdate",
                "Stock updated for product: ${product.productDescription}, New stock: $newStockLevel"
            )

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

                        Log.d(
                            "InventoryUpdate",
                            "Deducted $soldQuantity from product $productId, new quantity: $newQuantity"
                        )
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
            Log.d("ProductUpdate", "Product updated: $products")
        }
    }

    fun updatePriceList(priceList: PriceList) = viewModelScope.launch {
        try {
            priceListRepository.updatePriceList(priceList)
            Log.d("PriceListUpdate", "Price list updated successfully")
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

            Log.d("Invoice", "Starting invoice submission for customerId: $customerId")

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

                Log.d("Invoice", "Inserted Invoice with id: $invoiceId")

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
                        invoiceDate = System.currentTimeMillis()
                    )

                    Log.d("Invoice", "Inserting this invoiceLine: $invoiceLine")
                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
                    Log.d("Invoice", "Successfully Inserted this invoiceLine:")
                }

                _selectedProducts.value = emptyList()
                Log.d("Invoice", "Cleared selected products for customer: $customerId")
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
    fun submitInvoice(
        customerId: Long,
        amountTendered: Double,
        productQuantities: Map<Long, Int>,
        cashierEmail: String
    ) = viewModelScope.launch {
        try {
            val currencyCode = _selectedCurrency.value?.code ?: "USD"
            var totalAmount = 0.0

            Log.d("submitInvoice", "Currency: $currencyCode")
            Log.d("submitInvoice", "Amount tendered: $amountTendered")

            // Fetch customer details
            val customer = customerRepository.getCustomerById(customerId)
            if (customer == null) {
                Log.e("submitInvoice", "Customer not found for customerId: $customerId")
                return@launch
            }

            val selectedProducts = _selectedProducts.value
            Log.d(
                "submitInvoice",
                "Selected products: ${selectedProducts.map { it.productDescription }}"
            )

            if (selectedProducts.isEmpty()) {
                Log.e("submitInvoice", "No products selected.")
            }

            val exchangeRate = _selectedCurrency.value?.rateToBase ?: 1.0
            Log.d("submitInvoice", "Exchange rate to base: $exchangeRate")

            val selectedProductsWithDetails = selectedProducts.map { product ->
                val quantitySold = productQuantities[product.productId] ?: 1
                val priceList = priceListRepository.getPriceListByProductId(product.productId)

                if (priceList == null) {
                    Log.w(
                        "submitInvoice",
                        "No price list found for productId: ${product.productId}"
                    )
                }

                val basePrice = priceList?.sellingPrice ?: 0.0
                val finalPrice = basePrice * exchangeRate

                Log.d(
                    "submitInvoice",
                    "Product: ${product.productDescription}, Qty: $quantitySold, BasePrice: $basePrice, FinalPrice: $finalPrice"
                )

                totalAmount += finalPrice * quantitySold

                Triple(product, finalPrice, quantitySold)
            }

            Log.d("submitInvoice", "Total calculated amount: $totalAmount")

            val change = amountTendered - totalAmount
            Log.d("submitInvoice", "Calculated change: $change")

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
                onInvoiceCreatedOrUpdated(invoice)

                Log.d("submitInvoice", "Inserted Invoice with id: $invoiceId")

                deductProductQuantities(productQuantities)

                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
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
                        invoiceDate = System.currentTimeMillis()
                    )

                    Log.d("submitInvoice", "Inserting invoiceLine: $invoiceLine")
                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
                    Log.d(
                        "submitInvoice",
                        "Inserted invoiceLine for product: ${product.productDescription}"
                    )
                }

                _selectedProducts.value = emptyList()
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

//    fun submitInvoice(
//        customerId: Long,
//        amountTendered: Double,
//        productQuantities: Map<Long, Int>,
//        cashierEmail: String
//    ) = viewModelScope.launch {
//        try {
//            val currencyCode = _selectedCurrency.value?.code ?: "USD"
//            var totalAmount = 0.0
//
//            // Fetch customer details
//            val customer = customerRepository.getCustomerById(customerId)
//            if (customer == null) {
//                Log.e("submitInvoice", "Customer not found for customerId: $customerId")
//                return@launch
//            }
//
//            // Fetch selected products and calculate total amount based on quantities
//            val exchangeRate = _selectedCurrency.value?.rateToBase ?: 1.0
//            val selectedProductsWithDetails = _selectedProducts.value.map { product ->
//                val quantitySold = productQuantities[product.productId] ?: 1
//                val priceList = priceListRepository.getPriceListByProductId(product.productId)
//                val basePrice = priceList?.sellingPrice ?: 0.0
//                val finalPrice = basePrice * exchangeRate  // Apply the exchange rate
//                totalAmount += finalPrice * quantitySold // Use product-specific quantity for calculations
//
//                Triple(
//                    product,
//                    finalPrice,
//                    quantitySold
//                ) // Return a Triple of product, final price, and quantity
//            }
//
//            // Calculate change
//            val change = amountTendered - totalAmount
//            if (change < 0) {
//                Log.e("submitInvoice", "Amount tendered is less than total amount!")
//                return@launch
//            }
//
//            // Only proceed if there are valid selected products and totalAmount > 0
//            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
//                // Create an Invoice object
//                val invoice = Invoice(
//                    customerId = customerId,
//                    totalAmount = totalAmount,
//                    invoiceDate = System.currentTimeMillis(),
//                    dueDate = null,
//                    status = "Paid",
//                    currencyCode = currencyCode
//                )
//
//                // Insert the invoice and get the invoiceId
//                val invoiceId = invoiceRepository.insertInvoice(invoice)
//                onInvoiceCreatedOrUpdated(invoice)
//
//                Log.d("submitInvoice", "Inserted Invoice with id: $invoiceId")
//
//                // IMPORTANT: Deduct product quantities ONCE before the loop
//                // This prevents double deduction
//                deductProductQuantities(productQuantities)
//
//                // Insert CartItems, InvoiceItems for each selected product
//                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
//                    // Create CartItem record
//                    val cartItem = CartItem(
//                        customerId = customerId,
//                        productId = product.productId,
//                        invoiceId = invoiceId,
//                        totalAmount = finalPrice * quantitySold,
//                        totalAmountTendered = amountTendered,
//                        change = change
//                    )
//                    cartItemRepository.insertCartItem(cartItem)
//
//                    // Create InvoiceItem record
//                    val invoiceItem = InvoiceItem(
//                        invoiceId = invoiceId,
//                        productId = product.productId,
//                        customerId = customerId,
//                        quantity = quantitySold,
//                        price = finalPrice,
//                        total = finalPrice * quantitySold
//                    )
//                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
//                }
//
//                // Get the updated product quantities after deduction
//                val updatedProducts = mutableMapOf<Long, Double>()
//                for ((productId, quantitySold) in productQuantities) {
//                    val product = productRepository.getProductById(productId)
//                    updatedProducts[productId] = product?.productQty?.toDouble() ?: 0.0
//                }
//
//                // Insert InvoiceLine records for the report
//                selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
//                    val invoiceLine = InvoiceLine(
//                        invoiceId = invoiceId,
//                        invoiceStatus = invoice.status,
//                        productID = product.productId,
//                        productName = product.productDescription,
//                        productPrice = finalPrice,
//                        productQtySold = quantitySold,
//                        cashier = cashierEmail,
//                        customerID = customerId,
//                        customerName = customer.name,
//                        customerPhone = customer.phone,
//                        totalAmount = totalAmount,
//                        amountTendered = amountTendered,
//                        changeBalance = change,
//                        currentStock = updatedProducts[product.productId]
//                            ?: 0.0,  // Use the updated stock value
//                        invoiceDate = System.currentTimeMillis()
//                    )
//
//                    Log.d("submitInvoice", "Inserting this invoiceLine: $invoiceLine")
//                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
//                    Log.d("submitInvoice", "Successfully Inserted this invoiceLine:")
//                }
//
//                // Clear the selected products list after invoice submission
//                _selectedProducts.value = emptyList()
//                _selectedProducts.value = emptyList()
//            } else {
//                Log.e(
//                    "submitInvoice",
//                    "No valid selected products to invoice or total amount is 0"
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("submitInvoice", "Error during invoice submission", e)
//        }
//    }

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

                        Log.d(
                            "CreditNote",
                            "Updating credit note line: $updatedInvoiceLine"
                        )
                        invoiceLineRepository.updateInvoiceLine(updatedInvoiceLine)
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

                    Log.d(
                        "viewmodel",
                        "CurrentStockLevel is $updatedProduct \n and $updatedStockLevel"
                    )

                    // Save the updated product back to the repository
                    productRepository.updateProduct(updatedProduct)

                    Log.d(
                        "StockUpdate",
                        "Successfully added $quantity to product ID: $productId. Derived stock level: $totalQuantity"
                    )
                } else {
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
    private suspend fun onInvoiceCreatedOrUpdated(invoice: Invoice) {
        unsyncedCount++
        scheduleSingleInvoiceSync(invoice)

        if (unsyncedCount >= SYNC_BATCH_SIZE) {
            scheduleBatchSync()
            unsyncedCount = 0
        }
    }

    private fun scheduleSingleInvoiceSync(invoice: Invoice) {
        Log.d(TAG, "Scheduling sync for invoice ${invoice.invoiceId}")

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncInvoiceWorker>()
            .setInputData(
                workDataOf(
                    SyncInvoiceWorker.KEY_INVOICE_ID to invoice.invoiceId,
                    SyncInvoiceWorker.KEY_RETRY to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_invoice_${invoice.invoiceId}",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        viewModelScope.launch {
            checkAndScheduleBatchSyncIfNeeded()
        }
    }

    private suspend fun checkAndScheduleBatchSyncIfNeeded() {
        val currentUnsyncedCount = invoiceRepository.getUnsyncedCount().first()
        if (currentUnsyncedCount >= SYNC_BATCH_SIZE) {
            scheduleBatchSync()
            unsyncedCount = 0
        }
    }

    fun syncAllNow() {
        scheduleProductBatchSync()
        scheduleBatchSync()
        unsyncedCount = 0
    }

    private fun scheduleBatchSync() {
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
                15,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            BATCH_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            batchSyncWorkRequest
        )
    }

//    fun syncProducts() {
//        viewModelScope.launch {
//            try {
//                // First check if there are unsynced products
//                val unsyncedCount = productRepository.getUnsyncedProductsCount()
//                if (unsyncedCount > 0) {
//                    scheduleProductBatchSync()
//                    _uiState.update { it.copy(syncStatus = "Sync started for $unsyncedCount products") }
//                } else {
//                    _uiState.update { it.copy(syncStatus = "All products are synced") }
//                }
//            } catch (e: Exception) {
//                _uiState.update { it.copy(syncStatus = "Sync failed: ${e.localizedMessage}") }
//            }
//        }
//    }

    private fun scheduleProductBatchSync() {
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
                15,
                TimeUnit.MINUTES
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            PRODUCT_BATCH_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            batchSyncWorkRequest
        )
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

//    end syncInvoice

    private fun clearSelectedProducts() {
        _selectedProducts.value = emptyList()
    }

//    For invoice Batch processing
    fun processCsvInvoices(
        csvContent: String,
        cashierEmail: String
    ) = viewModelScope.launch {
        try {
            Log.d("debugCsv", "Starting CSV processing")

            val invoiceItems = parseCsvContent(csvContent)
            val validProducts = mutableListOf<Triple<Products, Int, Double>>()
            var customerId: Long? = null

            for (csvItem in invoiceItems) {
                val result = processSingleInvoiceLine(csvItem)
                result?.let { (product, qty, tendered) ->
                    validProducts.add(Triple(product, qty, tendered))
                    customerId = csvItem.customerId
                }
            }

            if (validProducts.isEmpty() || customerId == null) {
                Log.e("debugCsv", "❌ No valid products found or customerId missing.")
                return@launch
            }

            // Update _selectedProducts
            _selectedProducts.value = validProducts.map { it.first }
            Log.d("debugCsv", "✅ Selected products *****: ${_selectedProducts.value} ")

            // Map productId to qty
            val productQuantities = validProducts.associate { it.first.productId to it.second }

            // Total amount tendered
            val totalTendered = validProducts.sumOf { it.third }
            Log.d("debugCsv", "✅ Total tendered is  ===> $$totalTendered")

            submitInvoice(
                customerId = customerId!!,
                amountTendered = totalTendered,
                productQuantities = productQuantities,
                cashierEmail = cashierEmail
            )

            Log.d("debugCsv", "✅ Final invoice submitted with ${validProducts.size} products.")

        } catch (e: Exception) {
            Log.e("debugCsv", "Error processing CSV invoices", e)
        }
    }


    // CSV parsing remains the same
    private fun parseCsvContent(csvContent: String): List<CsvInvoiceItem> {
        return csvContent.lineSequence()
            .drop(1) // Skip header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                        .map { it.trim().removeSurrounding("\"") }

                    if (parts.size == 6) {
                        CsvInvoiceItem(
                            brand = parts[0].trim(),
                            size = parts[1].toDoubleOrNull() ?: 0.0,
                            description = parts[2].trim(),
                            quantity = parts[3].toIntOrNull() ?: 1,
                            amountTendered = parts[4].toDoubleOrNull() ?: 0.0,
                            customerId = parts[5].toLongOrNull() ?: 0L
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e("debugCsv", "Error parsing line: $line", e)
                    null
                }
            }
            .toList()
    }

    // single line processing with simplified size lookup
// Return Triple of Product, Quantity, TenderedAmount
    private suspend fun processSingleInvoiceLine(
        csvItem: CsvInvoiceItem
    ): Triple<Products, Int, Double>? {
        clearSelectedProducts()
        if (csvItem.customerId == 0L) {
            Log.e("debugCsv", "Invalid customer ID in line: $csvItem")
            return null
        }

        val brand = shoeBrandRepository.findBrandByNameExact(csvItem.brand)
            ?: shoeBrandRepository.findBrandByNameCaseInsensitive(csvItem.brand)
            ?: run {
                Log.e("debugCsv", "Brand not found: '${csvItem.brand}'")
                return null
            }

        val shoeSize = shoeSizeRepository.findSizeByValue(csvItem.size) ?: run {
            Log.e("debugCsv", "Size not found: ${csvItem.size}")
            return null
        }

        val product = findProductWithMultipleStrategies(
            brandId = brand.brandID,
            shoeSizeId = shoeSize.shoeSizeId,
            description = csvItem.description
        ) ?: run {
            Log.e("debugCsv", "Product not found: ${csvItem.description}")
            return null
        }

        return Triple(product, csvItem.quantity, csvItem.amountTendered)
    }

    // Product finding with multiple strategies
    private suspend fun findProductWithMultipleStrategies(
        brandId: Long,
        shoeSizeId: Long,
        description: String
    ): Products? {
        // Try exact match first
        var product = productRepository.getProductByExactMatch(brandId, shoeSizeId, description)
        Log.d("debugCsv", "Found product ByExactMatch is ===>: $product")

        // Try case-insensitive match
        if (product == null) {
            product =
                productRepository.getProductByCaseInsensitiveMatch(brandId, shoeSizeId, description)
            Log.d("debugCsv", "Found product ByCaseInsensitiveMatch is ===>: $product")
        }

        // Try contains match
        if (product == null) {
            product = productRepository.getProductByContainsMatch(brandId, shoeSizeId, description)
            Log.d("debugCsv", "Found product ByContainsMatch is ===>: $product")
        }

        return product
    }

//    end of batch invoice processing

    private val _studentDetailsList = MutableStateFlow(emptyList<StudentEntity>())
    val studentDetailsList = _studentDetailsList.asStateFlow()

    private val _loginStatus = MutableStateFlow<String>("")
    val loginStatus = _loginStatus.asStateFlow()

    fun getStudentDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            studentRepository.getAllStudents().collectLatest {
                _studentDetailsList.tryEmit(it)
            }
        }
    }

    fun updateStudent(studentEntity: StudentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            studentRepository.update(studentEntity)
        }
    }

    fun insertStudent(studentEntity: StudentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            studentRepository.insert(studentEntity)
        }
    }

    private val _studentName = MutableStateFlow("")
    val studentName = _studentName.asStateFlow()

    fun setStudentName(name: String) {
        _studentName.tryEmit(name)
    }

    private val _studentRollNo = MutableStateFlow("")
    val studentRollNo = _studentRollNo.asStateFlow()

    fun setStudentRollNo(roll: String) {
        _studentRollNo.tryEmit(roll)
    }

    private val _isChecked = MutableStateFlow(false)
    val isChecked = _isChecked.asStateFlow()

    fun setChecked(bool: Boolean) {
        _isChecked.tryEmit(bool)
    }

    // Login logic to check if student exists in the database
    fun loginStudent() {
        viewModelScope.launch(Dispatchers.IO) {
            val studentName = _studentName.value
            val studentRollNo = _studentRollNo.value

            // Query the database for a student with matching name and roll number
            val student =
                studentRepository.getStudentByNameAndRollNo(studentName, studentRollNo)

            // Update login status based on result
            if (student != null) {
                _loginStatus.tryEmit("Login success")
            } else {
                _loginStatus.tryEmit("Please sign up")
            }
        }
    }
}
