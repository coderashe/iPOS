package com.example.learncompose303.screens

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import java.io.File
import java.io.FileOutputStream

@Composable
fun CartScreen(
    customer: Customer,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Get the selected products
    val selectedProducts by viewModel.selectedProducts.collectAsState()

    // Collect product price details
    val productWithPriceDetails by viewModel.productsWithPriceDetails.collectAsState(initial = emptyList())
    val productDetails = productWithPriceDetails.joinToString("\n") { (product, price, quantity) ->
        "${product.productDescription}, $$price, Quantity: $quantity"
    }

    // State to hold the total amount tendered by the customer
    var amountTendered by remember { mutableStateOf("") }


    // Map to hold quantities for each product
    val productQuantities = remember { mutableStateMapOf<Long, Int>() }

    // Calculate totalPrice using sumOf for all product prices and quantities
    val totalPrice by remember {
        derivedStateOf {
            productWithPriceDetails.sumOf { (product, price) ->
                val quantity = productQuantities[product.productId] ?: 1
                price * quantity
            }
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
    val currencies by viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    // State for total price in base currency (USD)
    val baseTotalPrice by remember {
        derivedStateOf {
            productWithPriceDetails.sumOf { (product, price) ->
                val quantity = productQuantities[product.productId] ?: 1
                price * quantity
            }
        }
    }
    // Convert total price based on selected currency
    val exchangeRate = selectedCurrency?.rateToBase ?: 1.0
    val convertedTotalPrice = baseTotalPrice * exchangeRate

    // Load price details for selected products when the screen is entered
    LaunchedEffect(selectedProducts) {
        viewModel.getPriceDetailsForSelectedProducts(selectedProducts, productQuantities)
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
        // Currency Dropdown
        Box {
            Text("Select Currency")
            Button(onClick = { expanded = true }) {
                Text(selectedCurrency?.name ?: "Select Currency")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency.name) },
                        onClick = {
                            viewModel.selectCurrency(currency)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Total Amount: ${selectedCurrency?.code ?: "USD"} $convertedTotalPrice",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total Price: ${selectedCurrency?.code ?: "$"}${
                        "%.2f".format(
                            convertedTotalPrice
                        )
                    }",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Change: ${selectedCurrency?.code ?: "$"}${"%.2f".format(changeBalance * exchangeRate)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                OutlinedTextField(
                    value = amountTendered,
                    onValueChange = { newValue -> amountTendered = newValue },
                    label = { Text("Amount Tendered (${selectedCurrency?.code ?: "$"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

//        Row(modifier = Modifier.fillMaxWidth()) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Total Price: \$${totalPrice}",
//                    style = MaterialTheme.typography.titleSmall,
//                    color = MaterialTheme.colorScheme.primary
//                )
//
//                Text(
//                    text = "Change: \$${"%.2f".format(changeBalance)}",
//                    style = MaterialTheme.typography.bodySmall,
//                    fontSize = 16.sp
//                )
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            Column {
//                OutlinedTextField(
//                    value = amountTendered,
//                    onValueChange = { newValue -> amountTendered = newValue },
//                    label = { Text("Amount Tendered") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
//                )
//            }
//        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val tenderedAmount = amountTendered.toDoubleOrNull()

//                if (amountTendered.isNotEmpty() && tenderedAmount != null && tenderedAmount > 0) {
                if (tenderedAmount != null && tenderedAmount >= convertedTotalPrice) {

                    if (tenderedAmount >= totalPrice) {
                        // Use the actual quantities from productQuantities map
                        val productQuantitiesMap: Map<Long, Int> =
                            selectedProducts.associate { product ->
                                product.productId to (productQuantities[product.productId] ?: 1)
                            }

                        // Submit the invoice with individual product quantities
                        viewModel.submitInvoice(
                            customer.customerId,
                            tenderedAmount,
                            productQuantitiesMap,
                            cashierEmail
                        )

                        // Code for sharing the invoice message...
                        val message = "*INVOICE RECEIPT:*\n\n" +
                                "*CUSTOMER DETAILS*\n" +
                                "${customer.name}: ${customer.phone}\n\n" +
                                "*PRODUCTS PURCHASED:*\n" +
                                "$productDetails\n\n" +
                                "*SALES TOTALS*\n" +
                                "Total Amount: *$$totalPrice*\n" +
                                "Amount Tendered: *$$amountTendered*\n" +
                                "Change: *$${amountTendered.toDouble() - totalPrice}*\n\n" +
                                "THANK YOU!!!... powered by istartk \u00A92024"


                        try {
                            // Create a PDF document
                            val pdfDocument = PdfDocument()
                            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
                            val page = pdfDocument.startPage(pageInfo)

                            // Draw the invoice content on the page
                            val canvas: Canvas = page.canvas
                            val paint = Paint().apply { textSize = 12f }
                            var yPosition = 20
                            message.split("\n").forEach { line ->
                                canvas.drawText(line, 10f, yPosition.toFloat(), paint)
                                yPosition += 20
                            }

                            pdfDocument.finishPage(page)

                            // Save the PDF file
                            val pdfFile = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                "Invoice_${System.currentTimeMillis()}.pdf"
                            )
                            pdfDocument.writeTo(FileOutputStream(pdfFile))
                            pdfDocument.close()

                            // Share the PDF
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                                Log.e("Error*", "${e.message}")
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }

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
                    Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Submit Invoice")
        }

        Spacer(modifier = Modifier.height(16.dp))

// Display the list of selected products and their total cost
        LazyColumn {
            items(selectedProducts) { product ->

                // Find the product price from the collected productWithPriceDetails
                val productPrice =
                    productWithPriceDetails.find { it.first.productId == product.productId }?.second
                        ?: 0.0

                // Get the current quantity for this product from the map, default to 1 if not present
                val quantitySold = productQuantities[product.productId] ?: 1

                // Calculate the total price for the product
                val productTotalPrice = productPrice * quantitySold

                LaunchedEffect(quantitySold) {
                    productQuantities[product.productId] = quantitySold
                }

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
                                "\$${productPrice}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(horizontalAlignment = Alignment.End) {

                            // Input for quantity with better handling
                            OutlinedTextField(
                                value = if (quantitySold == 0) "" else quantitySold.toString(), // Show empty if quantity is 0
                                onValueChange = { newValue ->
                                    // Handle empty input and valid integers
                                    val newQuantity =
                                        newValue.toIntOrNull()?.takeIf { it >= 0 } ?: 0
                                    productQuantities[product.productId] = newQuantity
                                },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            // Display SubTotal
                            Text(
                                text = "SubTotal: \$${"%.2f".format(productTotalPrice)}", // Ensure proper formatting of price
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

    }

}
