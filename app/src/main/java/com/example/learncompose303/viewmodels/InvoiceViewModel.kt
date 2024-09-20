package com.example.learncompose303.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learncompose303.data.StudentEntity
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepository
import com.example.learncompose303.data.repositories.InvoiceRepository
import com.example.learncompose303.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val cartItemRepository: CartItemRepository
) : ViewModel() {

    // Customer list
    private val _customerList = MutableStateFlow(emptyList<Customer>())
    val customerList = _customerList.asStateFlow()

    // Product list
    private val _productList = MutableStateFlow(emptyList<Product>())
    val productList = _productList.asStateFlow()

    // Invoice items (InvoiceItem)
    private val _invoiceItems = MutableStateFlow(mutableListOf<InvoiceItem>())
    val invoiceItems = _invoiceItems.asStateFlow()

    // Cart items (CartItem)
    private val _cartItems = MutableStateFlow(emptyList<CartItem>())
    val cartItems = _cartItems.asStateFlow()

    init {
        insertInitialCustomers()
        fetchCustomers()
        insertInitialProducts()
        fetchProducts()
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

    // Insert initial products into the database
    private fun insertInitialProducts() = viewModelScope.launch {
        val existingProducts =
            productRepository.getAllProductsOnce() // Assuming you have this method implemented in the repository
        if (existingProducts.isEmpty()) {
            val initialProducts = listOf(
                Product(name = "Product 1", price = 10.0, imageResId = null),
                Product(name = "Product 2", price = 20.0, imageResId = null),
                Product(name = "Product 3", price = 30.0, imageResId = null),
                Product(name = "Product 4", price = 40.0, imageResId = null),
                Product(name = "Product 5", price = 50.0, imageResId = null),
                Product(name = "Product 6", price = 60.0, imageResId = null),
                Product(name = "Product 7", price = 70.0, imageResId = null),
                Product(name = "Product 8", price = 80.0, imageResId = null),
                Product(name = "Product 9", price = 90.0, imageResId = null),
                Product(name = "Product 10", price = 100.0, imageResId = null)
            )
            productRepository.insertProducts(initialProducts)
        }
    }

    // Fetch products from the database
    private fun fetchProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { productList ->
            _productList.value = productList
        }
    }

    // Fetch products from repository
    private fun findProducts() = viewModelScope.launch {
        productRepository.getAllProducts().collectLatest { _productList.value = it }
    }

    // Get customer by ID
    fun getCustomerById(customerId: Long?): Customer? {
        return customerList.value.firstOrNull { it.customerId == customerId }
    }

    // Add item to cart
    fun addProductToCart(customerId: Long, product: Product, quantity: Int) =
        viewModelScope.launch {
            val cartItem = CartItem(
                customerId = customerId,
                productId = product.productId,
                quantity = quantity
            )
            cartItemRepository.insertCartItem(cartItem)
            refreshCartItems(customerId)
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

    // Submit invoice (use cart items)
    fun submitInvoice(customerId: Long) = viewModelScope.launch {
        var totalAmount = 0.0

        // First, fetch the products for each cart item to calculate total amount
        val cartItemsWithProducts = _cartItems.value.mapNotNull { cartItem ->
            val product = productRepository.getProductById(cartItem.productId)

            // Safely check if product is null
            product?.let {
                totalAmount += it.price * cartItem.quantity
                cartItem to it // Return a pair of cartItem and product if product is not null
            }
        }

        // Insert the invoice
        val invoiceId = invoiceRepository.insertInvoice(
            Invoice(
                customerId = customerId,
                totalAmount = totalAmount,
                invoiceDate = System.currentTimeMillis(),
                dueDate = null,
                status = "Pending"
            )
        )

        // Convert CartItems to InvoiceItems and store them
        cartItemsWithProducts.forEach { (cartItem, product) ->
            val invoiceItem = InvoiceItem(
                invoiceId = invoiceId,
                productId = cartItem.productId, // Use productId here
                quantity = cartItem.quantity,
                price = product.price,
                total = product.price * cartItem.quantity
            )
            invoiceItemRepository.insertInvoiceItem(invoiceItem)
        }

        // Clear the cart for the customer after invoice submission
        clearCartForCustomer(customerId)
    }

}
