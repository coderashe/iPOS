package com.example.learncompose303.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.viewmodels.InvoiceViewModel
import java.util.Date

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoiceViewModel = viewModel(),
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) } // Show confirmation dialog
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Trigger fetching invoiceLines based on the invoiceId
    LaunchedEffect(key1 = invoiceId) {
        viewModel.fetchAllInvoiceLines()
        viewModel.fetchInvoiceLinesByInvoiceId(invoiceId)
    }

    // Collect the filtered invoiceLines
    val invoiceLines by viewModel.invoiceLines.collectAsState()
    val invoiceLine = viewModel.getInvoiceLineById(invoiceId)
    var isFabEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    // Build the order details string
    val linesDetails = invoiceLines.joinToString("\n") { invoiceLine ->
        "${invoiceLine.productName},     Qty: ${invoiceLine.productQtySold},     $${invoiceLine.productPrice}"
    }

//
    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirm Adding Shoe") },
            text = {
                Column {
                    Text("Are you sure you want to reverse this transaction?")
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = Color.Red)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle creditNote a product logic
                        // Check if the invoice line is not null and the status is not "Credit Note"
                        if (invoiceLine != null && invoiceLine.invoiceStatus != "Credit Note") {
                            if (isFabEnabled) {
                                // Trigger the reverseInvoice function
                                viewModel.reverseInvoice(invoiceId)
                                Toast.makeText(
                                    context,
                                    "Credit Note Successful!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                                // Disable the FAB once clicked
                                isFabEnabled = false
                                navController.navigate(Destinations.Reports.route)
                            }
                        }

                        // Close dialog after attempting to add
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.material.MaterialTheme.colors.primary)
                ) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            },
            backgroundColor = Color.White,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
//

    Box(modifier = Modifier.fillMaxSize()) {

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomHeadingMedium(
                    heading = "INVOICE RECEIPT",
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (invoiceLine != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = invoiceLine.customerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${invoiceLine.customerPhone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "#${invoiceLine.invoiceId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "${Date(invoiceLine.invoiceDate)}", // Formatting date can be customized
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Cashier: ${invoiceLine.cashier}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (invoiceLines.isNotEmpty()) {
                    // Display the invoice lines using LazyColumn
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(invoiceLines) { invoiceLine ->
                            TheInvoiceCard(
                                invoiceLine = invoiceLine,
                                viewModel = viewModel
                            )
                        }
                    }
                } else {
                    Text(text = "No items found for this invoice.")
                }
            }
        }

        // Reverse Invoice Button
        // Check if the invoice line is not null and the status is not "Credit Note"
        if (invoiceLine != null && invoiceLine.invoiceStatus != "Credit Note") {
            // Show the FAB if the invoice status is not "Credit Note"
            FloatingActionButton(
                onClick = {
//                    if (isFabEnabled) {
//                        // Trigger the reverseInvoice function
//                        viewModel.reverseInvoice(invoiceId)
//                        Toast.makeText(context, "Credit Note Successful!", Toast.LENGTH_SHORT)
//                            .show()
//
//                        // Disable the FAB once clicked
//                        isFabEnabled = false
//                        navController.navigate(Destinations.Reports.route)
//                    }
                    // Show confirmation dialog before reversing transaction
                    showDialog = true

                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp) // Add padding from edges
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Credit Note",
                    tint = Color.Red
                )
            }
        }

        // Floating Action Button for sending report
        FloatingActionButton(
            onClick = {
                // Code for sharing the invoice message...

                val message = "*${Date(System.currentTimeMillis())} REPORT:*\n\n\n" +
                        "*INVOICE RECEIPT: ${invoiceLine?.invoiceId}*\n" +
                        "Cashier: ${invoiceLine?.cashier}\n\n" +
                        "$linesDetails\n\n" +
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
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Add padding from edges
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send Report",
                tint = GreenColor
            )
        }
    }
}

@Composable
fun TheInvoiceCard(
    invoiceLine: InvoiceLine,
    viewModel: InvoiceViewModel
) {
    // First, fetch the actual product using the productID from invoiceLine
    var product by remember { mutableStateOf<Products?>(null) }

    // Get the product using productID
    LaunchedEffect(invoiceLine.productID) {
        viewModel.getSingleProductById(invoiceLine.productID).collect { fetchedProduct ->
            product = fetchedProduct
        }
    }

    // Now that we have the product, we can access its brandID and shoeSizeId
    var brand by remember { mutableStateOf<ShoeBrand?>(null) }
    var shoeSize by remember { mutableStateOf<ShoeSize?>(null) }

    // Once we have the product, fetch the brand using its brandID
    LaunchedEffect(product?.brandID) {
        product?.brandID?.let { brandId ->
            viewModel.getBrandById(brandId).collect { fetchedBrand ->
                brand = fetchedBrand
            }
        }
    }

    // Once we have the product, fetch the shoe size using its shoeSizeId
    LaunchedEffect(product?.shoeSizeId) {
        product?.shoeSizeId?.let { sizeId ->
            viewModel.getShoeSizeById(sizeId).collect { fetchedSize ->
                shoeSize = fetchedSize
            }
        }
    }

    val brandName = brand?.brandName ?: "Unknown Brand"
    val sizeValue = shoeSize?.size?.toString() ?: "N/A"

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = invoiceLine.productName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display brand name and shoe size
        Text(
            text = "$brandName :- $sizeValue",
            style = MaterialTheme.typography.bodySmall.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            color = Color.Red,
            modifier = Modifier.background(Color.White)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Price: ${invoiceLine.productPrice}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Qty Sold: ${invoiceLine.productQtySold}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}


//@Composable
//fun TheInvoiceCard(
//    invoiceLine: InvoiceLine,
//) {
//
//    Column(
//        modifier = Modifier.padding(16.dp)
//    ) {
//        Text(
//            text = invoiceLine.productName,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.primary
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "Price: ${invoiceLine.productPrice}",
//            style = MaterialTheme.typography.bodySmall
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = "Qty Sold: ${invoiceLine.productQtySold}",
//            style = MaterialTheme.typography.bodySmall
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Divider(
//            modifier = Modifier.padding(vertical = 8.dp),
//            color = MaterialTheme.colorScheme.primary
//        )
//    }
//
//}
