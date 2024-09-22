package com.example.learncompose303.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepository
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import com.example.learncompose303.data.repositories.InvoiceRepository
import com.example.learncompose303.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val invoiceItemRepository: InvoiceItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val invoiceLineRepository: InvoiceLineRepository
) : ViewModel() {

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

    // Cart items (CartItem)
    private val _cartItems = MutableStateFlow(emptyList<CartItem>())

    val cartItems = _cartItems.asStateFlow()

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
        fetchInvoices()
        insertInitialProducts()
        fetchProducts()
        fetchInvoiceLines()
        insertInitialCustomers()
        fetchCustomers()
    }

    private fun fetchInvoices() = viewModelScope.launch {
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

    // Insert initial products into the database
    private fun insertInitialProducts() = viewModelScope.launch {
        val existingProducts =
            productRepository.getAllProductsOnce() // Assuming you have this method implemented in the repository
        if (existingProducts.isEmpty()) {
            val initialProducts = listOf(
                Product(name = "Product 1", price = 10.0, imageResId = R.drawable.meat1),
                Product(name = "Product 2", price = 20.0, imageResId = R.drawable.chicken2),
                Product(name = "Product 3", price = 30.0, imageResId = R.drawable.meat3),
                Product(name = "Product 4", price = 40.0, imageResId = R.drawable.meat4),
                Product(name = "Product 5", price = 50.0, imageResId = R.drawable.chicken1),
                Product(name = "Product 6", price = 60.0, imageResId = R.drawable.meat2),
                Product(name = "Product 7", price = 70.0, imageResId = R.drawable.meat6),
                Product(name = "Product 8", price = 80.0, imageResId = R.drawable.meat7),
                Product(name = "Product 9", price = 90.0, imageResId = R.drawable.chicken1),
                Product(name = "Product 10", price = 100.0, imageResId = R.drawable.meat4)
            )
            productRepository.insertProducts(initialProducts)
        }
    }

    fun getInvoiceLineById(invoiceID: Long?): InvoiceLine? {
        return invoiceLineList.value.firstOrNull { it.invoiceID == invoiceID }
    }

    private fun fetchInvoiceLines() = viewModelScope.launch {
        invoiceLineRepository.getAllInvoiceLines().collectLatest { invoiceLineList ->
            _invoiceLineList.value = invoiceLineList
        }
    }

    // Fetch products from the database
    private fun fetchProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { productList ->
            _productList.value = productList
        }
    }

    fun insertProduct(product: Product) = viewModelScope.launch {
        productRepository.insertProduct(product)
    }

    // Add a product to the cart (selected products list)
    fun addProductToCart(product: Product) {
        val updatedList = _selectedProducts.value.toMutableList()
        updatedList.add(product)
        _selectedProducts.value = updatedList
    }


    // Fetch cart items for a customer
// Fetch cart items for a customer
    fun refreshCartItems(customerId: Long) = viewModelScope.launch {
        cartItemRepository.getCartItemsForCustomer(customerId).collectLatest { cartItems ->
            _cartItems.value = cartItems
        }
    }


    // Update quantity in the cart
    fun updateCartItem(cartItem: CartItem, newQuantity: Int) = viewModelScope.launch {
        val updatedItem = cartItem.copy(quantity = newQuantity)
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

    fun submitInvoice(customerId: Long) = viewModelScope.launch {
        try {
            var totalAmount = 0.0

            // Log the start of the operation
            Log.d("Invoice", "Starting invoice submission for customerId: $customerId")

            // Fetch selected products from the selectedProducts list and calculate total amount
            val selectedProductsWithDetails = _selectedProducts.value.map { product ->
                totalAmount += product.price // Assuming each product is counted as 1 quantity here
                Log.d(
                    "Invoice",
                    "Adding product: ${product.name}, price: ${product.price}, total so far: $totalAmount"
                )
                product
            }

            Log.d("Invoice", "Total Amount Calculated: $totalAmount")

            // Only proceed if there are valid selected products and totalAmount > 0
            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
                // Create an Invoice object
                val invoice = Invoice(
                    customerId = customerId,
                    totalAmount = totalAmount,
                    invoiceDate = System.currentTimeMillis(),
                    dueDate = null,
                    status = "Pending" // Assuming status is "Pending" for a new invoice
                )

                // Insert the invoice and get the invoiceId
                val invoiceId = invoiceRepository.insertInvoice(invoice) // Insert the invoice

                Log.d("Invoice", "Inserted Invoice with id: $invoiceId")

                // Convert selected products to InvoiceItems and store them
                selectedProductsWithDetails.forEach { product ->
                    val invoiceItem = InvoiceItem(
                        invoiceId = invoiceId,
                        productId = product.productId,
                        customerId = customerId,
                        quantity = 1, // Assuming 1 quantity per selected product (adjust as needed)
                        price = product.price,
                        total = product.price // Since quantity is 1
                    )
                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
                    Log.d("Invoice", "Inserted InvoiceItem: $invoiceItem")
                }

                // Now, insert a row into the InvoiceLine table for the submitted invoice
                val customer =
                    customerRepository.getCustomerById(customerId) // Fetch customer details
                selectedProductsWithDetails.forEach { product ->
                    val invoiceLine = customer?.let {
                        InvoiceLine(
                            invoiceID = invoiceId, // Get invoiceId from inserted invoice
                            invoiceStatus = invoice.status, // Get status from the inserted invoice object
                            productID = product.productId,
                            productName = product.name,
                            productPrice = product.price,
                            productQtySold = 1, // Assuming 1 quantity per product
                            customerID = customerId, // Set customerId
                            customerName = it.name,
                            customerPhone = customer.phone,
                            totalAmount = totalAmount,
                            amountTendered = null, // Add logic for amountTendered if applicable
                            changeBalance = null, // Add logic for changeBalance if applicable
                            invoiceDate = System.currentTimeMillis()
                        )
                    }
                    if (invoiceLine != null) {
                        invoiceLineRepository.insertInvoiceLine(invoiceLine) // Insert InvoiceLine record
                    }
                    Log.d("Invoice", "Inserted InvoiceLine: $invoiceLine")
                }

                // Clear the selected products list after invoice submission
                _selectedProducts.value = emptyList()
                Log.d("Invoice", "Cleared selected products for customer: $customerId")
            } else {
                Log.e("InvoiceError", "No valid selected products to invoice or total amount is 0")
            }
        } catch (e: Exception) {
            Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
        }
    }


    //
    // Submit invoice (use cart items)
//    fun submitInvoice(customerId: Long) = viewModelScope.launch {
//        try {
//            var totalAmount = 0.0
//
//            // Log the start of the operation
//            Log.d("Invoice", "Starting invoice submission for customerId: $customerId")
//
//            // Fetch selected products from the selectedProducts list and calculate total amount
//            val selectedProductsWithDetails = _selectedProducts.value.map { product ->
//                totalAmount += product.price // Assuming each product is counted as 1 quantity here
//                Log.d(
//                    "Invoice",
//                    "Adding product: ${product.name}, price: ${product.price}, total so far: $totalAmount"
//                )
//                product
//            }
//
//            Log.d("Invoice", "Total Amount Calculated: $totalAmount")
//
//            // Only proceed if there are valid selected products and totalAmount > 0
//            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
//                // Insert the invoice
//                val invoiceId = invoiceRepository.insertInvoice(
//                    Invoice(
//                        customerId = customerId,
//                        totalAmount = totalAmount,
//                        invoiceDate = System.currentTimeMillis(),
//                        dueDate = null,
//                        status = "Pending"
//                    )
//                )
//
//                Log.d("Invoice", "Inserted Invoice with id: $invoiceId")
//
//                // Convert selected products to InvoiceItems and store them
//                selectedProductsWithDetails.forEach { product ->
//                    val invoiceItem = InvoiceItem(
//                        invoiceId = invoiceId,
//                        productId = product.productId,
//                        customerId = customerId,
//                        quantity = 1, // Assuming 1 quantity per selected product (adjust as needed)
//                        price = product.price,
//                        total = product.price // Since quantity is 1
//                    )
//                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
//                    Log.d("Invoice", "Inserted InvoiceItem: $invoiceItem")
//                }
//
//                // Now, insert a row into the InvoiceLine table for the submitted invoice
//                // Fetch the customer details
//                val customer = customerRepository.getCustomerById(customerId) // Fetch customer details
//                selectedProductsWithDetails.forEach { product ->
//                    val invoiceLine = customer?.let {
//                        InvoiceLine(
//                            invoiceID = invoiceId, // Get invoiceId from inserted invoice
//                            invoiceStatus = "PAID", // Get status from the inserted invoice
//                            productID = product.productId,
//                            productName = product.name,
//                            productPrice = product.price,
//                            productQtySold = 1, // Assuming 1 quantity per product
//                            customerID = customerId, // Set customerId
//                            customerName = it.name,
//                            customerPhone = customer.phone,
//                            totalAmount = totalAmount,
//                            amountTendered = null, // Add logic for amountTendered if applicable
//                            changeBalance = null, // Add logic for changeBalance if applicable
//                            invoiceDate = System.currentTimeMillis()
//                        )
//                    }
//                    if (invoiceLine != null) {
//                        invoiceLineRepository.insertInvoiceLine(invoiceLine)
//                    } // Assuming this is the method for inserting into invoiceLines
//                    Log.d("Invoice", "Inserted InvoiceLine: $invoiceLine")
//                }
//
//                // Clear the selected products list after invoice submission
//                _selectedProducts.value = emptyList()
//                Log.d("Invoice", "Cleared selected products for customer: $customerId")
//            } else {
//                Log.e("InvoiceError", "No valid selected products to invoice or total amount is 0")
//            }
//        } catch (e: Exception) {
//            Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
//        }
//    }

}
