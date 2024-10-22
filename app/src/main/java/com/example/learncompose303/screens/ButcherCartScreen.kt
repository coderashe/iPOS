package com.example.learncompose303.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.viewmodels.InvoiceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalContext
import com.example.learncompose303.screens.auth.AuthViewModel

@Composable
fun ButcherCartScreen(
    customer: Customer,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Get the selected butcher products
    val selectedProducts by viewModel.selectedProducts.collectAsState()

    // Collect butcher product price details
    val productWithPriceDetails by viewModel.butcherInventoryWithPriceDetails.collectAsState(initial = emptyList())

    // Map to hold amount paid for each product
    val productAmountPaid = remember { mutableStateMapOf<Long, Double>() }
    val productSubQuantity =
        remember { mutableStateMapOf<Long, Double>() } // To hold subQuantity for each product

    // State to hold the total amount tendered by the customer
    var amountTendered by remember { mutableStateOf("") }

    // Calculate totalPrice using sumOf for all product prices and quantities
    val totalPrice by remember {
        derivedStateOf {
            productAmountPaid.values.sum()
        }
    }

    // State to hold the change balance
    val changeBalance = if (amountTendered.isNotEmpty()) {
        amountTendered.toDoubleOrNull()?.let { tendered ->
            tendered - totalPrice
        } ?: 0.0
    } else {
        0.0
    }

    // Get the cashierEmail from AuthViewModel
    val cashierEmail = authViewModel.userEmail.value ?: "unknown"

    // Load price details for selected products when the screen is entered
    LaunchedEffect(selectedProducts) {
        viewModel.getPriceDetailsForButcherSelectedProducts(selectedProducts, emptyMap())
    }

    // Initialize the amount paid for each product (price * quantity)
    LaunchedEffect(productWithPriceDetails) {
        productWithPriceDetails.forEach { (product, pricePerKg, defaultAmountPaid) ->
            productAmountPaid[product.productId] = defaultAmountPaid
            productSubQuantity[product.productId] =
                defaultAmountPaid / pricePerKg // Initialize subQuantity based on price
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                    label = { Text("Amount Tendered") },
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
                val tenderedAmount = amountTendered.toDoubleOrNull()

                if (amountTendered.isNotEmpty() && tenderedAmount != null && tenderedAmount > 0) {
                    if (tenderedAmount >= totalPrice) {
                        // Create a map to hold product IDs and their corresponding quantities
                        val productQuantities = selectedProducts.associate { product ->
                            product.productId to (productSubQuantity[product.productId] ?: 1.0)
                        }

                        // Submit the invoice with individual product quantities
                        viewModel.butcherSubmitInvoice(
                            customer.customerId,
                            tenderedAmount,
                            productQuantities,
                            cashierEmail
                        )

                        // Send the invoice receipt message
                        val message = "*BUTCHER INVOICE RECEIPT:*\n\n" +
                                "*CUSTOMER DETAILS*\n" +
                                "${customer.name}: ${customer.phone}\n\n" +
                                "*PRODUCTS PURCHASED:*\n" +
                                productWithPriceDetails.joinToString("\n") { (product, price, _) ->
                                    "${product.productDescription}, $$price/kg, Quantity: ${productSubQuantity[product.productId]}"
                                } +
                                "\n\n" +
                                "*SALES TOTALS*\n" +
                                "Total Amount: *$$totalPrice*\n" +
                                "Amount Tendered: *$$amountTendered*\n" +
                                "Change: *$${tenderedAmount - totalPrice}*\n\n" +
                                "THANK YOU!!!... powered by istartk \u00A92024"

                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }

                        try {
                            val chooserIntent = Intent.createChooser(
                                sendIntent,
                                "Send via WhatsApp or WhatsApp Business"
                            )
                            chooserIntent.resolveActivity(context.packageManager)?.let {
                                context.startActivity(chooserIntent)
                            } ?: run {
                                Toast.makeText(
                                    context,
                                    "No messaging app found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }

                        // Navigate and show success message
                        navController.navigate(Destinations.Rides.route) {
                            popUpTo(Destinations.Rides.route) {
                                inclusive = true
                            }
                        }

                        Toast.makeText(context, "Invoice paid successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Amount tendered must be greater than or equal to total price",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Submit Invoice")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(selectedProducts) { product ->

                // Find the product price (price per kg) from the collected productWithPriceDetails
                val pricePerKg =
                    productWithPriceDetails.find { it.first.productId == product.productId }?.second
                        ?: 0.0

                // Get the current amount paid and subQuantity for this product from the maps
                val amountPaid = productAmountPaid[product.productId] ?: 0.0
                val subQuantity = if (pricePerKg > 0) {
                    amountPaid / pricePerKg // Calculate subQuantity dynamically based on amountPaid and price
                } else {
                    0.0
                }
                productSubQuantity[product.productId] = subQuantity // Update subQuantity

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                product.productDescription,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "\$${pricePerKg}/kg",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            OutlinedTextField(
                                value = if (amountPaid == 0.0) "" else amountPaid.toString(), // Show empty if value is 0
                                onValueChange = { newValue ->
                                    // Check if the input is empty and allow it to be cleared
                                    val newAmountPaid = newValue.toDoubleOrNull() ?: 0.0
                                    productAmountPaid[product.productId] = newAmountPaid
                                },
                                label = { Text("Amount Paid (\$)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Text(
                                "Quantity (kg): ${"%.2f".format(subQuantity)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }


                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
