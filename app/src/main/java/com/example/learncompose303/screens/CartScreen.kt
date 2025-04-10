package com.example.learncompose303.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.viewmodels.InvoiceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.learncompose303.app.utils.BluetoothPrinterScanner
import com.example.learncompose303.screens.auth.AuthState
import com.example.learncompose303.screens.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.Date

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
//
    // Map to hold quantities for each product
    val productQuantities = remember { mutableStateMapOf<Long, Int>() }
    val productDetails = selectedProducts.joinToString("\n") { product ->
        val quantity = productQuantities[product.productId] ?: 1  // Fetch actual quantity
        val price =
            productWithPriceDetails.find { it.first.productId == product.productId }?.second ?: 0.0
        "${product.productDescription} :*$quantity :@ $$price"
    }
    // State to hold the total amount tendered by the customer
    var amountTendered by remember { mutableStateOf("") }
    // Calculate totalPrice using sumOf for all product prices and quantities
    val totalPrice by remember {
        derivedStateOf {
            productWithPriceDetails.sumOf { (product, price) ->
                val quantity = productQuantities[product.productId] ?: 1
                price * quantity
            }
        }
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
    // State to hold the change balance
    val changeBalance = if (amountTendered.isNotEmpty()) {
        amountTendered.toDoubleOrNull()?.let { tendered ->
            (tendered - convertedTotalPrice)
        } ?: 0.0
    } else {
        0.0
    }

    var theCurrency by remember { mutableStateOf("United States Dollar") } // Default currency -> USD

    // Load price details for selected products when the screen is entered
    LaunchedEffect(selectedProducts) {
        viewModel.getPriceDetailsForSelectedProducts(selectedProducts, productQuantities)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            //
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                // Currency Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = theCurrency,
                        onValueChange = {},
                        label = { Text(selectedCurrency?.name ?: "Select Currency") },
                        readOnly = true,
                        modifier = Modifier
                            .clickable { expanded = true },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                } // Also toggle using the icon
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency.name) },
                                onClick = {
                                    theCurrency = currency.name
                                    viewModel.selectCurrency(currency)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            //
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "Total: ${selectedCurrency?.code ?: "$"}${
                        "%.2f".format(
                            convertedTotalPrice
                        )
                    }",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
//                    modifier = Modifier.align(Alignment.End)
                )
            }
            //
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    customer.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            //
        }
        Divider(
            modifier = Modifier.padding(vertical = 2.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = amountTendered,
                    onValueChange = { newValue -> amountTendered = newValue },
                    label = { Text("Amount Tendered (${selectedCurrency?.code ?: "$"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(
                    text = "Change: ${selectedCurrency?.code ?: "$"}${"%.2f".format(changeBalance)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                // Observe auth state and navigate to sign-in page when unauthenticated
                val authState by authViewModel.authState.observeAsState()

                Button(
                    onClick = {
                        val tenderedAmount = amountTendered.toDoubleOrNull()

                        if (tenderedAmount != null && tenderedAmount >= convertedTotalPrice) {
                            if (authState == AuthState.Authenticated) {
                                // Use the actual quantities from productQuantities map
                                val productQuantitiesMap: Map<Long, Int> =
                                    selectedProducts.associate { product ->
                                        product.productId to (productQuantities[product.productId]
                                            ?: 1)
                                    }

                                // Submit the invoice with individual product quantities
                                viewModel.submitInvoice(
                                    customer.customerId,
                                    tenderedAmount,
                                    productQuantitiesMap,
                                    cashierEmail
                                )

                                Log.d("cartScreen", "Submitted qty is  ${productQuantitiesMap}")

                                // Share the invoice message...
                                val message = "RECEIPT: ${Date(System.currentTimeMillis())}\n\n" +
                                        "CUSTOMER DETAILS\n" +
                                        "${customer.name}: ${customer.phone}\n\n" +
                                        "PRODUCTS PURCHASED\n" +
                                        "$productDetails\n\n" +
                                        "SALES TOTALS\n" +
                                        "Total Cost: ${selectedCurrency?.code ?: "$"}$convertedTotalPrice\n" +
                                        "Tendered:   ${selectedCurrency?.code ?: "$"}$amountTendered\n" +
                                        "Change:     ${selectedCurrency?.code ?: "$"}${amountTendered.toDouble() - convertedTotalPrice}\n\n" +
                                        "THANK YOU!.. @iSTARTk \u00A92025"

                                // Start scanning
                                BluetoothPrinterScanner(context).connectToHardcodedPrinter(message)

                                // Generate and save the PDF
                                generateInvoicePdf(message, context)

                                // Navigate to CustomerSelectionScreen after successful invoice
                                navController.navigate(Destinations.CustomerSelection.route) {
                                    popUpTo(Destinations.Dashboard.route) {
                                        inclusive = true
                                    }
                                }

                                Toast.makeText(
                                    context,
                                    "Invoice paid successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Log out and navigate to SignIn screen if not authenticated
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Destinations.SignIn.route) {
                                    popUpTo(Destinations.SignIn.route) { inclusive = true }
                                }
                                Log.d("AuthViewModel", "Auth status is $authState")

                                Toast.makeText(
                                    context,
                                    "You are not authenticated. Please sign in.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Amount tendered must be greater than or equal to total price",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                ) {
                    Text("PAY")
                }
            }

//            Column(
//                modifier = Modifier
//                    .align(Alignment.CenterVertically)
//            ) {
//                // Observe auth state and navigate to sign-in page when unauthenticated
//                Button(
//                    onClick = {
//                        val tenderedAmount = amountTendered.toDoubleOrNull()
////                if (amountTendered.isNotEmpty() && tenderedAmount != null && tenderedAmount > 0) {
//                        if (tenderedAmount != null && tenderedAmount >= convertedTotalPrice) {
//
//                            if (tenderedAmount >= convertedTotalPrice) {
//                                // Use the actual quantities from productQuantities map
//                                val productQuantitiesMap: Map<Long, Int> =
//                                    selectedProducts.associate { product ->
//                                        product.productId to (productQuantities[product.productId]
//                                            ?: 1)
//                                    }
//                                // Submit the invoice with individual product quantities
//                                viewModel.submitInvoice(
//                                    customer.customerId,
//                                    tenderedAmount,
//                                    productQuantitiesMap,
//                                    cashierEmail
//                                )
//
//                                // Code for sharing the invoice message...
//                                val message = "RECEIPT: ${Date(System.currentTimeMillis())}\n\n" +
//                                        "CUSTOMER DETAILS\n" +
//                                        "${customer.name}: ${customer.phone}\n\n" +
//                                        "PRODUCTS PURCHASED\n" +
//                                        "$productDetails\n\n" +
//                                        "SALES TOTALS\n" +
//                                        "Total Cost: ${selectedCurrency?.code ?: "$"}$convertedTotalPrice\n" +
//                                        "Tendered:   ${selectedCurrency?.code ?: "$"}$amountTendered\n" +
//                                        "Change:     ${selectedCurrency?.code ?: "$"}${amountTendered.toDouble() - convertedTotalPrice}\n\n" +
//                                        "THANK YOU!.. @iSTARTk \u00A92025"
//                                // Start scanning
//                                BluetoothPrinterScanner(context).connectToHardcodedPrinter(message)
//                                try {
//                                    // Create a PDF document
//                                    val pdfDocument = PdfDocument()
//                                    val pageInfo =
//                                        PdfDocument.PageInfo.Builder(300, 600, 1).create()
//                                    val page = pdfDocument.startPage(pageInfo)
//
//                                    // Draw the invoice content on the page
//                                    val canvas: Canvas = page.canvas
//                                    val paint = Paint().apply { textSize = 12f }
//                                    var yPosition = 20
//                                    message.split("\n").forEach { line ->
//                                        canvas.drawText(line, 10f, yPosition.toFloat(), paint)
//                                        yPosition += 20
//                                    }
//                                    pdfDocument.finishPage(page)
//                                    // Save the PDF file
//                                    val pdfFile = File(
//                                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//                                        "Invoice_${System.currentTimeMillis()}.pdf"
//                                    )
//                                    pdfDocument.writeTo(FileOutputStream(pdfFile))
//                                    pdfDocument.close()
//
//                                    // Share the PDF
//                                    val uri = androidx.core.content.FileProvider.getUriForFile(
//                                        context,
//                                        "${context.packageName}.fileprovider",
//                                        pdfFile
//                                    )
//                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                                        type = "application/pdf"
//                                        putExtra(Intent.EXTRA_STREAM, uri)
//                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                                    }
//                                    try {
//
//                                        val chooserIntent = Intent.createChooser(
//                                            sendIntent,
//                                            "Send via WhatsApp or WhatsApp Business"
//                                        )
//                                        chooserIntent.resolveActivity(context.packageManager)?.let {
//                                            context.startActivity(chooserIntent)
//                                        } ?: run {
//                                            Toast.makeText(
//                                                context,
//                                                "No messaging app found",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    } catch (e: Exception) {
//                                        Log.e("Error*", "${e.message}")
//                                        Toast.makeText(
//                                            context,
//                                            "Error: ${e.message}",
//                                            Toast.LENGTH_SHORT
//                                        )
//                                            .show()
//                                    }
//
//                                } catch (e: Exception) {
//                                    Toast.makeText(
//                                        context,
//                                        "Error: ${e.message}",
//                                        Toast.LENGTH_SHORT
//                                    )
//                                        .show()
//                                }
//                                navController.navigate(Destinations.CustomerSelection.route) {
//                                    popUpTo(Destinations.Rides.route) {
//                                        inclusive = true
//                                    }
//                                }
//                                Toast.makeText(
//                                    context,
//                                    "Invoice paid successfully",
//                                    Toast.LENGTH_SHORT
//                                )
//                                    .show()
//
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "Amount tendered must be greater than or equal to total price",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "Please enter valid details",
//                                Toast.LENGTH_SHORT
//                            )
//                                .show()
//                        }
//                    },
////            modifier = Modifier.fillMaxWidth(),
//                ) {
//                    Text("PAY")
//                }
//            }


        }
        Divider(
            modifier = Modifier.padding(vertical = 2.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))

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
                                    val newQuantity = newValue.toIntOrNull()?.takeIf { it >= 0 } ?: 0

                                    // Get the current product's stock level
                                    val currentProduct = productWithPriceDetails.find { it.first.productId == product.productId }?.first
                                    val stockAvailable = currentProduct?.productQty ?: 0

                                    // Make sure both values are of the same type (Int)
                                    val validQuantity = if (newQuantity > stockAvailable) stockAvailable else newQuantity

                                    // Update the quantity map with the validated value
                                    productQuantities[product.productId] = validQuantity.toInt()

                                    // Optional: Show toast if user tried to enter more than stock
                                    if (newQuantity > stockAvailable) {
                                        Toast.makeText(
                                            context,
                                            "Only $stockAvailable units available in stock",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
//                            OutlinedTextField(
//                                value = if (quantitySold == 0) "" else quantitySold.toString(), // Show empty if quantity is 0
//                                onValueChange = { newValue ->
//                                    // Handle empty input and valid integers
//                                    val newQuantity =
//                                        newValue.toIntOrNull()?.takeIf { it >= 0 } ?: 0
//                                    productQuantities[product.productId] = newQuantity
//                                },
//                                label = { Text("Quantity") },
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                shape = RoundedCornerShape(8.dp),
//                                singleLine = true
//                            )

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

private fun generateInvoicePdf(message: String, context: Context) {

    try {
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
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
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
        Toast.makeText(
            context,
            "Error: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
