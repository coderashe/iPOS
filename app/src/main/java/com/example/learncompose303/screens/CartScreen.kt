package com.example.learncompose303.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.stringResource
import com.example.learncompose303.R
import com.example.learncompose303.ui.theme.Gray1
import com.example.learncompose303.ui.theme.LightGray
import com.example.learncompose303.ui.theme.MyGray

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
        Text("For: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
//

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total Price: \$${totalPrice}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Change: \$${changeBalance}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                OutlinedTextField(
                    value = amountTendered,
                    onValueChange = { newValue -> amountTendered = newValue },
                    label = { Text(stringResource(R.string.amount_tendered)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.submitInvoice(customer.customerId)
                // Navigate to the receipt screen or another destination after submitting the invoice
                navController.navigate(Destinations.Rides.route) // Replace this with your actual destination for receipt
            },
            modifier = Modifier
                .fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(MyGray)
        ) {
            Text("Submit Invoice")
        }
//

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of selected products and their total cost
        LazyColumn(
//            modifier = Modifier.height(400.dp)
        ) {
            items(selectedProducts) { product ->
                var quantity by remember { mutableStateOf(1) } // State to store quantity

                val productTotalPrice = product.price * quantity

                // Update total price whenever quantity changes
                LaunchedEffect(quantity) {
                    totalPrice = selectedProducts.sumOf { it.price * quantity }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
//        colors = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
//                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "\$${product.price}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            // Input for quantity
                            OutlinedTextField(
                                value = quantity.toString(),
                                onValueChange = { newValue ->
                                    quantity = newValue.toIntOrNull() ?: 1
                                },
                                label = { Text(text = stringResource(R.string.quantity)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Text(
                                text = "SubTotal: \$${productTotalPrice}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

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
