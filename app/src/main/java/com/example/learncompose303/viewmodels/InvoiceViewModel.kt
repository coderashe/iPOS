package com.example.learncompose303.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepository
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import com.example.learncompose303.data.repositories.InvoiceRepository
import com.example.learncompose303.data.repositories.OrderRepository
import com.example.learncompose303.data.repositories.PriceListRepository
import com.example.learncompose303.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private val priceListRepository: PriceListRepository
) : ViewModel() {

    // Use Triple to hold product, price, and quantity
    private val _productWithPriceDetails =
        MutableStateFlow<List<Triple<Product, Double, Int>>>(emptyList())
    val productWithPriceDetails: StateFlow<List<Triple<Product, Double, Int>>> =
        _productWithPriceDetails


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
    private val _productList = MutableStateFlow(emptyList<Product>())
    val productList = _productList.asStateFlow()

    private val _invoiceLineList = MutableStateFlow(emptyList<InvoiceLine>())
    val invoiceLineList = _invoiceLineList.asStateFlow()

    // List to store selected products for the cart
    private val _selectedProducts = MutableStateFlow<List<Product>>(emptyList())
    val selectedProducts: StateFlow<List<Product>> = _selectedProducts

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
//      Essential db operations to load data
        fetchPriceList()
        loadInvoices()
        insertInitialProducts()
        fetchProducts()
        fetchInvoiceLines()
        insertInitialCustomers()
        fetchCustomers()
        fetchAllOrders()
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

    // Modify to return a Triple with product, price, and quantity
    fun getPriceDetailsForSelectedProducts(
        selectedProducts: List<Product>,
        productQuantities: Map<Long, Int>
    ) {
        viewModelScope.launch {
            val productDetailsWithPrices = selectedProducts.map { product ->
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
            _productWithPriceDetails.value = productDetailsWithPrices
        }
    }


    private fun loadInvoices() = viewModelScope.launch {
        invoiceRepository.getAllInvoices().collectLatest { invoiceList ->
            _invoiceList.value = invoiceList
        }
    }

    // Fetch invoiceLines by invoiceId from repository
    fun fetchInvoiceLinesByInvoiceId(invoiceId: Long) = viewModelScope.launch {
        val invoiceLineList = invoiceLineRepository.getInvoiceLinesByInvoiceId(invoiceId)
        _invoiceLines.value = invoiceLineList
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

    // Fetch products from the database
    private fun fetchProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { productList ->
            _productList.value = productList
        }
    }

    val defaultImage = R.drawable.meat3

    // Insert initial products into the database
    private fun insertInitialProducts() = viewModelScope.launch {
        val existingProducts = productRepository.getAllProductsOnce()

        if (existingProducts.isEmpty()) {
            val initialProducts = listOf(
                Product(
                    productDescription = "Product 1",
                    currentStockLevel = 100.00,
                    saleable = true,
                    purchasable = true,
                    averageCost = 8.0,
                    standardCost = 10.0,
                    salesTaxCode = "VAT",
                    purchasableTaxCode = "VAT",
                    inclusiveTaxed = true,
                ),
                Product(
                    productDescription = "Product 2",
                    currentStockLevel = 150.00,
                    saleable = true,
                    purchasable = true,
                    averageCost = 15.0,
                    standardCost = 20.0,
                    salesTaxCode = "VAT",
                    purchasableTaxCode = "VAT",
                    inclusiveTaxed = true
                ),
                Product(
                    productDescription = "Product 3",
                    currentStockLevel = 200.00,
                    saleable = true,
                    purchasable = true,
                    averageCost = 25.0,
                    standardCost = 30.0,
                    salesTaxCode = "VAT",
                    purchasableTaxCode = "VAT",
                    inclusiveTaxed = true
                ),
                Product(
                    productDescription = "Product 4",
                    currentStockLevel = 250.00,
                    saleable = true,
                    purchasable = true,
                    averageCost = 35.0,
                    standardCost = 40.0,
                    salesTaxCode = "VAT",
                    purchasableTaxCode = "VAT",
                    inclusiveTaxed = true
                ),
                Product(
                    productDescription = "Product 5",
                    currentStockLevel = 300.00,
                    saleable = true,
                    purchasable = true,
                    averageCost = 45.0,
                    standardCost = 50.0,
                    salesTaxCode = "VAT",
                    purchasableTaxCode = "VAT",
                    inclusiveTaxed = true
                )
            )

            // Insert initial products and get the inserted IDs
            val productIds = productRepository.insertProducts(initialProducts)

            // After inserting products, insert into PriceList table
            productIds.forEachIndexed { index, productId ->
                val product = initialProducts[index]
                val sellingPrice = product.standardCost * 1.2 // Example of a 20% markup
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


    fun insertProduct(product: Product) = viewModelScope.launch {
        productRepository.insertProduct(product)
    }

    // Add a product to the cart (selected products list)
    fun addProductToCart(product: Product) {
        val updatedList = _selectedProducts.value.toMutableList()
        if (updatedList.contains(product)) {
            updatedList.remove(product) // Deselect if already added
        } else {
            updatedList.add(product) // Add if not already selected
        }
        _selectedProducts.value = updatedList
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
        Log.d("InvoiceViewModel", "Total Sales: $totalSales, Transaction Count: $transactionCount")

        return totalSales to transactionCount
    }


    fun getInvoiceLineById(invoiceID: Long?): InvoiceLine? {
        return invoiceLineList.value.firstOrNull { it.invoiceID == invoiceID }
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
                notes = notes
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
                Log.e("StockUpdateError", "Product with productId: $productId is not saleable")
                return
            }

            // Check if there is enough stock to sell the desired quantity
            if (product.currentStockLevel < quantitySold) {
                Log.e(
                    "StockUpdateError",
                    "Insufficient stock for product: ${product.productDescription}. " +
                            "Available: ${product.currentStockLevel}, Required: $quantitySold"
                )
                return
            }

            // Deduct the quantity sold from the current stock level
            val newStockLevel = product.currentStockLevel - quantitySold

            // Create a new product object with the updated stock level
            val updatedProduct = product.copy(currentStockLevel = newStockLevel)

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


    //    new submit
    fun submitInvoice(customerId: Long, amountTendered: Double, productQuantities: Map<Long, Int>) =
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
                    val priceList = priceListRepository.getPriceListByProductId(product.productId)
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
                            invoiceID = invoiceId,
                            invoiceStatus = invoice.status,
                            productID = product.productId,
                            productName = product.productDescription,
                            productPrice = finalPrice,
                            productQtySold = quantitySold,
                            customerID = customerId,
                            customerName = customer.name,
                            customerPhone = customer.phone,
                            totalAmount = totalAmount,
                            amountTendered = amountTendered,
                            changeBalance = change,
                            invoiceDate = System.currentTimeMillis()
                        )
                        invoiceLineRepository.insertInvoiceLine(invoiceLine)
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
}
