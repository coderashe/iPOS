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
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun CartScreen(
    customer: Customer,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    // Get the selected products
    val selectedProducts by viewModel.selectedProducts.collectAsState()

    // State to hold the total amount tendered by the customer
    var amountTendered by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf(0.0) }

    // State to hold the change balance
    val changeBalance = if (amountTendered.isNotEmpty()) {
        amountTendered.toDoubleOrNull()?.let { tendered ->
            tendered - totalPrice
        } ?: 0.0
    } else {
        0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Transparent)
        Spacer(modifier = Modifier.height(16.dp))
//

        // Display the total price of all selected products
        Text(
            text = "Total Price: \$${totalPrice}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display the change balance
        Text(
            text = "Change: \$${changeBalance}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // TextField for entering the amount tendered by the customer
        TextField(
            value = amountTendered,
            onValueChange = { newValue -> amountTendered = newValue },
            label = { Text("Amount Tendered") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate(Destinations.Rides.route) // Navigate to InvoiceScreen
            },
            modifier = Modifier.fillMaxWidth()
            //            .align(Alignment.End)
        ) {
            Text("Finalize Invoice")
        }
//

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of selected products and their total cost
        LazyColumn(
            modifier = Modifier.height(400.dp)
        ) {
            items(selectedProducts) { product ->
                var quantity by remember { mutableStateOf(1) } // State to store quantity

                val productTotalPrice = product.price * quantity

                // Update total price whenever quantity changes
                LaunchedEffect(quantity) {
                    totalPrice = selectedProducts.sumOf { it.price * quantity }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(product.name)
                        Text("Price: \$${product.price}")
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {

                        // Input for quantity
                        TextField(
                            value = quantity.toString(),
                            onValueChange = { newValue ->
                                quantity = newValue.toIntOrNull() ?: 1
                            },
//                            modifier = Modifier.width(130.dp)
                        )

                        // Display the total cost for each product
                        Text(
                            text = "Total: \$${productTotalPrice}",
                            textAlign = TextAlign.End
//                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

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
