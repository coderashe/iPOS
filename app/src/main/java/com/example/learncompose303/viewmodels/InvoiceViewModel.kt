package com.example.learncompose303.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.R
import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.CsvParser
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.DayReports
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ProductDayReport
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
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
import com.example.learncompose303.data.repositories.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject


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
    private val productDayReportRepository: ProductDayReportRepository
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
        // Now insert initial products
//        insertInitialProducts()
//        fetchProducts()
    }

    // Function to fetch the stock level for a product based on brandId
    suspend fun fetchStockLevel(brandId: Long): Int {
        return shoeSizeRepository.getTotalQuantityByBrand(brandId)
    }

    // Fetch current stock by brandID
    suspend fun getCurrentStockByBrand(brandID: Long): Int {
        return shoeSizeRepository.getTotalQuantityByBrand(brandID)
    }

    // Fetch all shoe sizes
    fun fetchShoeSizes() = viewModelScope.launch {
        shoeSizeRepository.getAllShoeSizes().collectLatest { shoeSizesList ->
            _shoeSizes.value = shoeSizesList
            Log.d("istart", "Updated Shoe size list: $shoeSizesList")
        }
    }

    // Fetch all shoe brands
    fun fetchShoeBrands() = viewModelScope.launch {
        shoeBrandRepository.getAllShoeBrands().collectLatest { shoeBrandsList ->
            _shoeBrands.value = shoeBrandsList
            Log.d("istart", "Updated Shoe brand list: $shoeBrandsList")
        }
    }


    // Logic for uploading products from CSV
    fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
        viewModelScope.launch {
            val parser = CsvParser()
            val productsFromCsv = parser.parseProductCsv(inputStream)

            // Retrieve existing products from the database using the once method
            val existingProducts = productRepository.getAllProductsOnce()

            // Create a map to track existing products by their description
            val existingProductMap = existingProducts.associateBy { it.productDescription }

            val duplicateCount = 0

            for (productCsv in productsFromCsv) {
                val existingProduct = existingProductMap[productCsv.productDescription]

                // If the product exists, update its details
                if (existingProduct != null) {
                    // Update the existing product details
                    val updatedProduct = existingProduct.copy(
                        saleable = productCsv.saleable,
                        purchasable = productCsv.purchasable,
                        averageCost = productCsv.averageCost,
                        standardCost = productCsv.standardCost ?: existingProduct.standardCost,
                        salesTaxCode = productCsv.salesTaxCode,
                        productCode = "",
                        inclusiveTaxed = productCsv.inclusiveTaxed,
                        imageUri = productCsv.imageUri
                            ?: existingProduct.imageUri // Retain existing image URI if null
                    )

                    // Update the product in the repository
                    productRepository.updateProduct(updatedProduct)

                    // Update the price list for the existing product
                    val sellingPrice = updatedProduct.standardCost * 1 // Adjust as needed
                    val markupPercentage =
                        ((sellingPrice - updatedProduct.standardCost) / updatedProduct.standardCost) * 100
                    val grossProfitPercentage =
                        ((sellingPrice - updatedProduct.averageCost) / sellingPrice) * 100

                    val priceList = PriceList(
                        productId = updatedProduct.productId, // Use the existing productId
                        productDescription = updatedProduct.productDescription,
                        unitCost = updatedProduct.standardCost,
                        sellingPrice = sellingPrice,
                        markupPercentage = markupPercentage,
                        grossProfitPercentage = grossProfitPercentage
                    )

                    // Update the price list in the repository
                    priceListRepository.updatePriceList(priceList)

                } else {
                    // Product does not exist, add it
                    val imageUri = productCsv.imageUri ?: getDefaultImageUri(context)
                    val standardCost = productCsv.standardCost ?: 0.0

                    // Create a new product entity
                    val newProducts = Products(
                        productDescription = productCsv.productDescription,
                        saleable = productCsv.saleable,
                        purchasable = productCsv.purchasable,
                        averageCost = productCsv.averageCost,
                        standardCost = standardCost,
                        salesTaxCode = productCsv.salesTaxCode,
                        productCode = "",
                        inclusiveTaxed = productCsv.inclusiveTaxed,
                        shoeSizeId = 1,
                        brandID = 1,
                        storeID = "ISTART01_1",
                        imageUri = imageUri
                    )

                    // Insert new product into the repository
                    val productId = productRepository.insertProduct(newProducts)

                    // Insert into PriceList for the new product
                    val sellingPrice = standardCost * 1 // Adjust as needed
                    val markupPercentage = ((sellingPrice - standardCost) / standardCost) * 100
                    val grossProfitPercentage =
                        ((sellingPrice - productCsv.averageCost) / sellingPrice) * 100

                    val priceList = PriceList(
                        productId = productId, // Use the newly inserted productId
                        productDescription = productCsv.productDescription,
                        unitCost = standardCost,
                        sellingPrice = sellingPrice,
                        markupPercentage = markupPercentage,
                        grossProfitPercentage = grossProfitPercentage
                    )

                    // Insert the price list for the new product
                    priceListRepository.insertPriceList(priceList)
                }
            }

            // Show toast notification
            if (duplicateCount > 0) {
                Toast.makeText(
                    context,
                    "$duplicateCount duplicate products were found and skipped.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Products uploaded successfully.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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
        Log.e("istartlogs", "Fetching invoice lines for invoiceId: $invoiceId")
        val invoiceLineList = invoiceLineRepository.getInvoiceLinesByInvoiceId(invoiceId)
        Log.e("istartlogs", "Fetched invoice lines: $invoiceLineList")
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
                Customer(1, "Simba Start", "john@example.com", "123456789", "State House"),
                Customer(2, "Jane Smith", "jane@example.com", "987654321", "State House"),
                Customer(3, "John Doe", "jane@example.com", "987654321", "State House"),
                Customer(4, "Amy Petersen", "jane@example.com", "987654321", "State House"),
                Customer(5, "Amara Brown", "jane@example.com", "987654321", "State House"),
                Customer(6, "walkInCustomer", "cashsale@istart.com", "0776815030", null),
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
        return customerList.value.firstOrNull { it.name == "walkInCustomer" }
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
                storeID = "ISTART01",
                userEmail = "dr.simbastart@gmail.com",
                userAddress = "3 Maywood St, Kwekwe, Zimbabwe"
            )

            authRepository.signUpUser(initialUser)
        }
    }


    // Insert initial shoe sizes
    private fun insertInitialShoeSizes() = viewModelScope.launch {
        val existingSizes = shoeSizeRepository.getAllShoeSizesOnce()

        if (existingSizes.isEmpty()) {
            val initialSizes = listOf(
                ShoeSize(1, 1, 6.0, 20),
                ShoeSize(2, 1, 5.0, 30),
                ShoeSize(3, 2, 7.0, 40),
                ShoeSize(4, 3, 9.0, 50),
                ShoeSize(5, 2, 8.5, 60),
                ShoeSize(6, 1, 4.0, 70),
                ShoeSize(7, 3, 6.5, 80),
            )
            shoeSizeRepository.insertShoeSizes(initialSizes)
        }
    }

    // Insert initial shoe brands
    private fun insertInitialShoeBrands() = viewModelScope.launch {
        val existingBrands = shoeBrandRepository.getAllShoeBrandsOnce()

        if (existingBrands.isEmpty()) {
            val initialBrands = listOf(
                ShoeBrand(1, "Nike"),
                ShoeBrand(2, "Adidas"),
                ShoeBrand(3, "Puma")
            )
            shoeBrandRepository.insertShoeBrands(initialBrands)
        }
    }


    // Insert initial products with ShoeSize and ShoeBrand associations
    private fun insertInitialProducts() = viewModelScope.launch {
        val existingProducts = productRepository.getAllProductsOnce()

        if (existingProducts.isEmpty()) {
            // Example products with associated sizes and brands
            val initialProducts = listOf(
                Products(
                    productDescription = "Nike Air Max",
                    saleable = true,
                    purchasable = true,
                    averageCost = 75.00,
                    standardCost = 90.00,
                    salesTaxCode = "VAT",
                    productCode = "NA001",
                    inclusiveTaxed = true,
                    shoeSizeId = 1,
                    brandID = 1,
                    storeID = "ISTART01_2"
                ),
                Products(
                    productDescription = "Adidas Ultraboost",
                    saleable = true,
                    purchasable = true,
                    averageCost = 100.00,
                    standardCost = 120.00,
                    salesTaxCode = "VAT",
                    productCode = "AU001",
                    inclusiveTaxed = true,
                    shoeSizeId = 2,
                    brandID = 2,
                    storeID = "ISTART01_1"
                )
            )

            // Insert products and update the PriceList
            val productIds = productRepository.insertProducts(initialProducts)

            productIds.forEachIndexed { index, productId ->
                val product = initialProducts[index]
                val sellingPrice = product.standardCost * 1.2
                val markupPercentage =
                    ((sellingPrice - product.standardCost) / product.standardCost) * 100
                val grossProfitPercentage =
                    ((sellingPrice - product.averageCost) / sellingPrice) * 100

                val priceList = PriceList(
                    productId = productId,
                    productDescription = product.productDescription,
                    unitCost = product.standardCost,
                    sellingPrice = sellingPrice,
                    markupPercentage = markupPercentage,
                    grossProfitPercentage = grossProfitPercentage
                )
                priceListRepository.insertPriceList(priceList)
            }
        }
    }

    // Fetch products from the database
    fun fetchProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { productList ->
            _productsList.value = productList
        }
    }

//

    // Insert initial products into the database
//    private fun insertInitialProducts() = viewModelScope.launch {
//        val existingProducts = productRepository.getAllProductsOnce()
//
//        if (existingProducts.isEmpty()) {
//            val initialInventories = listOf(
//                Products(
//                    productDescription = "Beef",
//                    saleable = true,
//                    purchasable = true,
//                    averageCost = 7.50,
//                    standardCost = 8.00,
//                    salesTaxCode = "VAT",
//                    productCode = "VAT",
//                    inclusiveTaxed = true,
//                ),
//                Products(
//                    productDescription = "Coke",
//                    saleable = true,
//                    purchasable = true,
//                    averageCost = 6.50,
//                    standardCost = 7.00,
//                    salesTaxCode = "VAT",
//                    productCode = "VAT",
//                    inclusiveTaxed = true
//                ),
//                Products(
//                    productDescription = "Chips",
//                    saleable = true,
//                    purchasable = true,
//                    averageCost = 3.60,
//                    standardCost = 4.00,
//                    salesTaxCode = "VAT",
//                    productCode = "VAT",
//                    inclusiveTaxed = true
//                ),
//                Products(
//                    productDescription = "iPhone",
//                    saleable = true,
//                    purchasable = true,
//                    averageCost = 8.50,
//                    standardCost = 9.00,
//                    salesTaxCode = "VAT",
//                    productCode = "VAT",
//                    inclusiveTaxed = true
//                ),
//                Products(
//                    productDescription = "Jersey",
//                    saleable = true,
//                    purchasable = true,
//                    averageCost = 4.50,
//                    standardCost = 5.0,
//                    salesTaxCode = "VAT",
//                    productCode = "VAT",
//                    inclusiveTaxed = true
//                )
//            )
//
//            // Insert initial products and get the inserted IDs
//            val productIds = productRepository.insertProducts(initialInventories)
//
//            // After inserting products, insert into PriceList table
//            productIds.forEachIndexed { index, productId ->
//                val product = initialInventories[index]
////                val sellingPrice = product.standardCost * 1.2 // Example of a 20% markup
//                val sellingPrice = product.standardCost * 1
//                val markupPercentage =
//                    ((sellingPrice - product.standardCost) / product.standardCost) * 100
//                val grossProfitPercentage =
//                    ((sellingPrice - product.averageCost) / sellingPrice) * 100
//
//                val priceList = PriceList(
//                    productId = productId,
//                    productDescription = product.productDescription,
//                    unitCost = product.standardCost,
//                    sellingPrice = sellingPrice,
//                    markupPercentage = markupPercentage,
//                    grossProfitPercentage = grossProfitPercentage
//                )
//                priceListRepository.insertPriceList(priceList)
//            }
//        }
//    }

    fun insertTheProduct(products: Products, onProductInserted: (Long) -> Unit) =
        viewModelScope.launch {
            val productId = productRepository.insertProduct(products)
            onProductInserted(productId)
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodaySalesInfo(): Pair<Double, Int> {
        val today = LocalDate.now()

        // Log the current date for reference
        Log.d("InvoiceViewModel", "Today's Date: $today")

        // Log the entire invoice list for debugging
        Log.d("InvoiceViewModel", "Invoice List: ${_invoiceList.value}")

        // Filter invoices for today
        val todayInvoices = _invoiceList.value.filter { invoice ->
            // Convert the invoiceDate timestamp (assuming it's in milliseconds) to LocalDate
            val invoiceDate =
                LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000)) // Ensure this is in milliseconds

            // Log each invoice date being checked
            Log.d(
                "InvoiceViewModel",
                "Invoice Date: ${invoice.invoiceDate}, LocalDate: $invoiceDate"
            )

            // Check if the invoice date matches today
            invoiceDate == today
        }

        // Calculate total sales and transaction count
        val totalSales = todayInvoices.sumOf { it.totalAmount }
        val transactionCount = todayInvoices.size

        // Log the filtered invoices for debugging
        Log.d("InvoiceViewModel", "Today's Invoices: $todayInvoices")
        Log.d(
            "InvoiceViewModel",
            "Total Sales: $totalSales, Transaction Count: $transactionCount"
        )

        return totalSales to transactionCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeeklySalesInfo(): Pair<Double, Int> {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        val endOfWeek = today.with(DayOfWeek.SUNDAY)

        // Log the week range
        Log.d("InvoiceViewModel", "Week Range: $startOfWeek to $endOfWeek")

        // Filter invoices for the current week
        val weeklyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate =
                LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))

            // Log each invoice date being checked
            Log.d("InvoiceViewModel", "Invoice Date: $invoiceDate")

            invoiceDate.isAfter(startOfWeek.minusDays(1)) && invoiceDate.isBefore(
                endOfWeek.plusDays(
                    1
                )
            )
        }

        // Calculate total sales and transaction count
        val totalSales = weeklyInvoices.sumOf { it.totalAmount }
        val transactionCount = weeklyInvoices.size

        // Log the filtered invoices for debugging
        Log.d("InvoiceViewModel", "Weekly Invoices: $weeklyInvoices")
        Log.d(
            "InvoiceViewModel",
            "Total Sales: $totalSales, Transaction Count: $transactionCount"
        )

        return totalSales to transactionCount
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlySalesInfo(): Pair<Double, Int> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        // Log the month range
        Log.d("InvoiceViewModel", "Month Range: $startOfMonth to $endOfMonth")

        // Filter invoices for the current month
        val monthlyInvoices = _invoiceList.value.filter { invoice ->
            val invoiceDate =
                LocalDate.ofEpochDay(invoice.invoiceDate / (24 * 60 * 60 * 1000))

            // Log each invoice date being checked
            Log.d("InvoiceViewModel", "Invoice Date: $invoiceDate")

            invoiceDate.isAfter(startOfMonth.minusDays(1)) && invoiceDate.isBefore(
                endOfMonth.plusDays(
                    1
                )
            )
        }

        // Calculate total sales and transaction count
        val totalSales = monthlyInvoices.sumOf { it.totalAmount }
        val transactionCount = monthlyInvoices.size

        // Log the filtered invoices for debugging
        Log.d("InvoiceViewModel", "Monthly Invoices: $monthlyInvoices")
        Log.d(
            "InvoiceViewModel",
            "Total Sales: $totalSales, Transaction Count: $transactionCount"
        )

        return totalSales to transactionCount
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

    // Fetch specific invoice by ID
    fun getInvoiceById(invoiceId: Long): Invoice? {
        return invoiceList.value.firstOrNull { it.invoiceId == invoiceId }
    }

    suspend fun deductButcherQuantityAndUpdateStock(productId: Long, quantitySold: Double) {
        try {
            // Fetch the product from the repository
            val product = productRepository.getProductById(productId)

            // Check if the product exists
            if (product == null) {
                Log.e("StockUpdateError", "Product not found with productId: $productId")
                return
            }

            // Check if the product is saleable
            if (!product.saleable) {
                Log.e(
                    "StockUpdateError",
                    "Product with productId: $productId is not saleable"
                )
                return
            }

            // Fetch the related ShoeBrand and ShoeSize entities (if applicable to butcher logic)
            val shoeBrand = shoeBrandRepository.getShoeBrandById(product.brandID)
            val shoeSize = shoeSizeRepository.getShoeSizeById(product.shoeSizeId)

            if (shoeBrand == null || shoeSize == null) {
                Log.e(
                    "StockUpdateError",
                    "ShoeBrand or ShoeSize not found for productId: $productId"
                )
                return
            }

            // Calculate the actual stock level by summing the quantity of all ShoeSizes for the brand
            val totalStockLevel = shoeSizeRepository.getTotalQuantityByBrand(shoeBrand.brandID)

            // Check if there is enough stock to sell the desired quantity
            if (totalStockLevel < quantitySold) {
                Log.e(
                    "StockUpdateError",
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
                "StockUpdateError",
                "Error while updating stock for productId: $productId: ${e.message}"
            )
        }
    }


//    suspend fun deductQuantityAndUpdateStock(productId: Long, quantitySold: Int) {
//        try {
//            // Fetch the product from the repository
//            val product = productRepository.getProductById(productId)
//
//            // Check if the product exists
//            if (product == null) {
//                Log.e("StockUpdateError", "Product not found with productId: $productId")
//                return
//            }
//
//            // Check if the product is saleable
//            if (!product.saleable) {
//                Log.e(
//                    "StockUpdateError",
//                    "Product with productId: $productId is not saleable"
//                )
//                return
//            }
//
//            // Check if there is enough stock to sell the desired quantity
//            if (product.currentStockLevel < quantitySold) {
//                Log.e(
//                    "StockUpdateError",
//                    "Insufficient stock for product: ${product.productDescription}. " +
//                            "Available: , Required: $quantitySold"
//                )
//                return
//            }
//
//            // Deduct the quantity sold from the current stock level
//            val newStockLevel = product.currentStockLevel - quantitySold
//
//            // Create a new product object with the updated stock level
//            val updatedProduct = product.copy(newStockLevel)
//
//            // Update the product in the database
//            productRepository.updateProduct(updatedProduct)
//
//            Log.d(
//                "StockUpdate",
//                "Stock updated for product: ${product.productDescription}, New stock: $newStockLevel"
//            )
//
//        } catch (e: Exception) {
//            Log.e(
//                "StockUpdateError",
//                "Error while updating stock for productId: $productId: ${e.message}"
//            )
//        }
//    }

    suspend fun deductQuantityAndUpdateStock(productId: Long, quantitySold: Int) {
        try {
            // Fetch the product from the repository
            val product = productRepository.getProductById(productId)

            // Check if the product exists
            if (product == null) {
                Log.e("StockUpdateError", "Product not found with productId: $productId")
                return
            }

            // Check if the product is saleable
            if (!product.saleable) {
                Log.e(
                    "StockUpdateError",
                    "Product with productId: $productId is not saleable"
                )
                return
            }

            // Fetch the related ShoeBrand and ShoeSize entities
            val shoeBrand = shoeBrandRepository.getShoeBrandById(product.brandID)
            val shoeSize = shoeSizeRepository.getShoeSizeById(product.shoeSizeId)

            if (shoeBrand == null || shoeSize == null) {
                Log.e(
                    "StockUpdateError",
                    "ShoeBrand or ShoeSize not found for productId: $productId"
                )
                return
            }

            // Calculate the actual stock level by summing the quantity of all ShoeSizes for the brand
            val totalStockLevel = shoeSizeRepository.getTotalQuantityByBrand(shoeBrand.brandID)

            // Check if there is enough stock to sell the desired quantity
            if (totalStockLevel < quantitySold) {
                Log.e(
                    "StockUpdateError",
                    "Insufficient stock for product: ${product.productDescription}. " +
                            "Available: $totalStockLevel, Required: $quantitySold"
                )
                return
            }

            // Deduct the quantity sold from the total stock level
            val newStockLevel = totalStockLevel - quantitySold

            // Create a new product object with the updated stock level
            val updatedProduct = product.copy(newStockLevel.toLong())

            // Update the product in the database
            productRepository.updateProduct(updatedProduct)

            Log.d(
                "StockUpdate",
                "Stock updated for product: ${product.productDescription}, New stock: $newStockLevel"
            )

        } catch (e: Exception) {
            Log.e(
                "StockUpdateError",
                "Error while updating stock for productId: $productId: ${e.message}"
            )
        }
    }


//    fun startDay() = viewModelScope.launch {
//        // If the day has already started, return early and avoid re-running the logic
//        if (_dayStarted.value) return@launch
//
//        productRepository.getAllProducts().collect { products ->
//            products.forEach { product ->
//                // Set the opening stock at the start of the day
//                val productDayReport = ProductDayReport(
//                    productId = product.productId,
//                    productName = product.productDescription,
//                    openingStock = product.currentStockLevel, // Opening stock set at day start
//                    closingStock = 0.0,
//                    quantitySold = 0, // No sales at the start of the day
//                    totalSales = 0.0,
//                    totalCost = 0.0,
//                    totalProfit = 0.0,
//                    reportDate = System.currentTimeMillis()
//                )
//                productDayReportRepository.insertProductDayReport(productDayReport)
//            }
//
//            // Create a day report with initial values
//            val dayReport = DayReports(
//                totalInvoices = 0,
//                totalOrders = 0,
//                totalProductsSold = 0,
//                totalInvoiceSales = 0.0,
//                reportDate = System.currentTimeMillis(),
//                startTime = System.currentTimeMillis(),
//                endTime = 0L // This will be updated at the end of the day
//            )
//            dayReportRepository.insertDayReport(dayReport)
//
//            _dayStarted.value = true // Mark the day as started
//        }
//    }

    fun getTodayStartInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

//    fun endDay() = viewModelScope.launch {
//        // Fetch all invoices and orders from today
//        val todayStart = getTodayStartInMillis()
//
//        // Collect today's invoices
//        invoiceRepository.getInvoicesByDate(todayStart).collect { todayInvoices ->
//            // Collect today's orders
//            orderRepository.getOrderByDate(todayStart).collect { todayOrders ->
//
//                // Total products sold and total sales
//                var totalProductsSold = 0
//                var totalSales = 0.0
//
//                // Iterate through each invoice
//                todayInvoices.forEach { invoice ->
//                    totalSales += invoice.totalAmount
//
//                    // Collect invoice items for this invoice
//                    invoiceItemRepository.getInvoiceItemsByInvoiceId(invoice.invoiceId)
//                        .collect { invoiceItems ->
//                            invoiceItems.forEach { item ->
//                                totalProductsSold += item.quantity
//
//                                // Fetch the product details
//                                val product =
//                                    productRepository.getProductById(item.productId)
//
//                                // Fetch the product day report to update it
//                                val productReport = product?.let {
//                                    productDayReportRepository.getProductDayReportById(
//                                        it.productId
//                                    )
//                                }
//
//                                if (productReport != null) {
//                                    // Calculate profit
//                                    val productProfit =
//                                        (item.price - (product.standardCost
//                                            ?: 0.0)) * item.quantity
//
//                                    // Update product report details
//                                    val updatedReport = productReport.copy(
//                                        closingStock = product.currentStockLevel, // Now this line will work
//                                        quantitySold = productReport.quantitySold + item.quantity,
//                                        totalSales = productReport.totalSales + item.total,
//                                        totalCost = productReport.totalCost + (product.standardCost
//                                            ?: 0.0) * item.quantity,
//                                        totalProfit = productReport.totalProfit + productProfit
//                                    )
//
//                                    // Update the product day report in the database
//                                    productDayReportRepository.updateProductDayReport(
//                                        updatedReport
//                                    )
//                                }
//                            }
//                        }
//                }
//
//                // Create a final day report summary
//                val finalDayReport = DayReports(
//                    totalInvoices = todayInvoices.size,
//                    totalOrders = todayOrders.size,
//                    totalProductsSold = totalProductsSold,
//                    totalInvoiceSales = totalSales,
//                    reportDate = todayStart,
//                    startTime = todayStart,
//                    endTime = System.currentTimeMillis()
//                )
//
//                // Update the final day report in the database
//                dayReportRepository.updateDayReport(finalDayReport)
//            }
//        }
//
//        _dayStarted.value = false // Update the state to indicate the day has ended
//    }

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
                    status = "Paid"
                )

                val invoiceId = invoiceRepository.insertInvoice(invoice)

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
                        currentStock = quantitySold, // Adjust as necessary
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


    fun submitInvoice(
        customerId: Long,
        amountTendered: Double,
        productQuantities: Map<Long, Int>,
        cashierEmail: String
    ) =
        viewModelScope.launch {
            try {
                var totalAmount = 0.0
                val taxPercentage = 0.15 // Example tax percentage
                val discountPercentage = 0.0 // Example discount percentage

                // Log the start of the operation
                Log.d("Invoice", "Starting invoice submission for customerId: $customerId")

                // Fetch customer details
                val customer = customerRepository.getCustomerById(customerId)
                if (customer == null) {
                    Log.e("InvoiceError", "Customer not found for customerId: $customerId")
                    return@launch
                }

                // Fetch selected products and calculate total amount based on quantities
                val selectedProductsWithDetails = _selectedProducts.value.map { product ->
                    val quantitySold = productQuantities[product.productId] ?: 1
                    // Fetch the corresponding price from the PriceList entity
                    val priceList =
                        priceListRepository.getPriceListByProductId(product.productId)
                    val basePrice = priceList?.sellingPrice ?: 0.0
                    val taxAmount = basePrice * taxPercentage
                    val discountAmount = basePrice * discountPercentage

                    val finalPrice = basePrice
                    totalAmount += finalPrice * quantitySold // Use product-specific quantity for calculations

                    Triple(
                        product,
                        finalPrice,
                        quantitySold
                    ) // Return a Triple of product, final price, and quantity
                }

                // Calculate change
                val change = amountTendered - totalAmount
                if (change < 0) {
                    Log.e("InvoiceError", "Amount tendered is less than total amount!")
                    return@launch
                }

                // Only proceed if there are valid selected products and totalAmount > 0
                if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
                    // Create an Invoice object
                    val invoice = Invoice(
                        customerId = customerId,
                        totalAmount = totalAmount,
                        invoiceDate = System.currentTimeMillis(),
                        dueDate = null,
                        status = "Paid"
                    )

                    // Insert the invoice and get the invoiceId
                    val invoiceId = invoiceRepository.insertInvoice(invoice)

                    Log.d("Invoice", "Inserted Invoice with id: $invoiceId")

                    // Insert CartItems, InvoiceItems, and deduct stock for each selected product
                    selectedProductsWithDetails.forEach { (product, finalPrice, quantitySold) ->
                        // Deduct the sold quantity from stock
                        deductQuantityAndUpdateStock(product.productId, quantitySold)

                        // Create CartItem record
                        val cartItem = CartItem(
                            customerId = customerId,
                            productId = product.productId,
                            invoiceId = invoiceId,
                            totalAmount = finalPrice * quantitySold,
                            totalAmountTendered = amountTendered,
                            change = change
                        )
                        cartItemRepository.insertCartItem(cartItem)

                        // Create InvoiceItem record
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

                    // Insert InvoiceLine records for the report
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
//                            currentStock =  .minus(quantitySold),
                            currentStock = quantitySold.toDouble(),
                            invoiceDate = System.currentTimeMillis()
                        )

                        Log.d("istart Invoice", "Inserting this invoiceLine: $invoiceLine")
                        invoiceLineRepository.insertInvoiceLine(invoiceLine)
                        Log.d("istart Invoice", "Succesfully Inserted this invoiceLine:")
                    }

                    // Clear the selected products list after invoice submission
                    _selectedProducts.value = emptyList()
                    Log.d("Invoice", "Cleared selected products for customer: $customerId")
                } else {
                    Log.e(
                        "InvoiceError",
                        "No valid selected products to invoice or total amount is 0"
                    )
                }
            } catch (e: Exception) {
                Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
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
                        "istart",
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
                        "StockUpdateError",
                        "ShoeBrand or ShoeSize not found for product ID: $productId"
                    )
                }
            } else {
                Log.e("StockUpdateError", "Product not found with ID: $productId")
            }
        } catch (e: Exception) {
            Log.e(
                "StockUpdateError",
                "Error updating stock for product ID: $productId, ${e.message}"
            )
        }
    }

//    fun addQuantityBackToStock(productId: Long, quantity: Double) = viewModelScope.launch {
//        try {
//            // Fetch the product by productId
//            val product = productRepository.getProductById(productId)
//
//            if (product != null) {
//                // Update the stock level by adding the quantity back
//                val updatedStockLevel = product.currentStockLevel + quantity
//                val updatedProduct = product.copy(currentStockLevel = updatedStockLevel)
//
//                // Save the updated product back to the repository
//                productRepository.updateProduct(updatedProduct)
//
//                Log.d(
//                    "StockUpdate",
//                    "Successfully added $quantity to product ID: $productId"
//                )
//            } else {
//                Log.e("StockUpdateError", "Product not found with ID: $productId")
//            }
//        } catch (e: Exception) {
//            Log.e(
//                "StockUpdateError",
//                "Error updating stock for product ID: $productId, ${e.message}"
//            )
//        }
//    }


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
