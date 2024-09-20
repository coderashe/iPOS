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
import androidx.compose.material3.TextField
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.viewmodels.InvoiceViewModel

//@Composable
//fun CartScreen(
//    customer: Customer,
//    cartItems: List<CartItem>,
//    products: List<Product>, // Pass the list of products
//    onQuantityChange: (CartItem, Int) -> Unit,
//    onCompleteInvoice: () -> Unit
//) {
//    val cartItemsWithProducts = cartItems.mapNotNull { cartItem ->
//        val product = products.find { it.productId == cartItem.productId }
//        product?.let { cartItem to it }
//    }
//
//    val totalPrice = cartItemsWithProducts.sumOf { (cartItem, product) ->
//        product.price * cartItem.quantity
//    }
//
//    // UI for displaying cart items
//    Column {
//
//        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Total Price: \$${totalPrice}")
//        cartItemsWithProducts.forEach { (cartItem, product) ->
//            CartItemCard(
//                cartItem = cartItem,
//                product = product,
//                onQuantityChange = onQuantityChange
//            )
//        }
//
//        Button(onClick = onCompleteInvoice) {
//            Text("Complete Invoice")
//        }
//    }
//}

@Composable
fun CartScreen(
    customer: Customer,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    // Get the selected products
    val selectedProducts by viewModel.selectedProducts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of selected products
        LazyColumn {
            items(selectedProducts) { product ->
                var quantity by remember { mutableStateOf(1) } // State to store quantity

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(product.name, modifier = Modifier.weight(1f))
                    Text("Price: \$${product.price}")
                    Spacer(modifier = Modifier.width(16.dp))

                    // Input for quantity
                    TextField(
                        value = quantity.toString(),
                        onValueChange = { newValue -> quantity = newValue.toIntOrNull() ?: 1 },
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                navController.navigate(Destinations.Rides.route) // Navigate to InvoiceScreen
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Finalize Invoice")
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
            Text("Quantity: ${cartItem.quantity}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

    val viewModel: InvoiceViewModel = viewModel()
    val navController = rememberNavController()

    // Sample data
    val customer =
        Customer(
            customerId = 1,
            name = "John Doe",
            email = "john.doe@example.com",
            "0769853534",
            "State House"
        )

    CartScreen(
        customer = customer,
        navController,
        viewModel
    )
}
