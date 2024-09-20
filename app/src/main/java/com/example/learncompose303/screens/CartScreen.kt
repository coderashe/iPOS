package com.example.learncompose303.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun CartScreen(
    customer: Customer,
    cartItems: List<CartItem>,
    products: List<Product>, // Pass the list of products
    onQuantityChange: (CartItem, Int) -> Unit,
    onCompleteInvoice: () -> Unit
) {
    val cartItemsWithProducts = cartItems.mapNotNull { cartItem ->
        val product = products.find { it.productId == cartItem.productId }
        product?.let { cartItem to it }
    }

    val totalPrice = cartItemsWithProducts.sumOf { (cartItem, product) ->
        product.price * cartItem.quantity
    }

    // UI for displaying cart items
    Column {

        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Price: \$${totalPrice}")
        cartItemsWithProducts.forEach { (cartItem, product) ->
            CartItemCard(
                cartItem = cartItem,
                product = product,
                onQuantityChange = onQuantityChange
            )
        }

        Button(onClick = onCompleteInvoice) {
            Text("Complete Invoice")
        }
    }
}

@Composable
fun CartItemCard(cartItem: CartItem, product: Product, onQuantityChange: (CartItem, Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.name, fontWeight = FontWeight.Bold) // Use product name
            Text("Price: \$${product.price}") // Use product price
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quantity: ${cartItem.quantity}")
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onQuantityChange(cartItem, cartItem.quantity + 1) }) {
                    Text("+")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onQuantityChange(cartItem, cartItem.quantity - 1) }) {
                    Text("-")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCartScreen() {
    // Sample data
    val customer =
        Customer(
            customerId = 1,
            name = "John Doe",
            email = "john.doe@example.com",
            "0769853534",
            "State House"
        )

    val products = listOf(
        Product(productId = 1, name = "Product A", price = 10.0),
        Product(productId = 2, name = "Product B", price = 20.0)
    )

    val cartItems = listOf(
        CartItem(cartItemId = 1, customerId = 1, productId = 1, quantity = 2),
        CartItem(cartItemId = 2, customerId = 1, productId = 2, quantity = 1)
    )

    // Dummy functions for preview
    val onQuantityChange: (CartItem, Int) -> Unit = { _, _ -> }
    val onCompleteInvoice: () -> Unit = {}

    CartScreen(
        customer = customer,
        cartItems = cartItems,
        products = products,
        onQuantityChange = onQuantityChange,
        onCompleteInvoice = onCompleteInvoice
    )
}
