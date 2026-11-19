package com.ipos.screens

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
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ipos.app.CustomHeadingMedium
import com.ipos.app.utils.Utils
import com.ipos.data.Destinations
import com.ipos.data.entities.Invoice
import com.ipos.data.entities.InvoiceLine
import kotlinx.coroutines.launch
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
import com.ipos.ui.theme.GreenColor
import com.ipos.viewmodels.InvoiceViewModel
import java.util.Date

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoiceViewModel = viewModel(),
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    val errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var invoice by remember { mutableStateOf<Invoice?>(null) }

    // Trigger fetching invoiceLines and invoice based on the invoiceId
    LaunchedEffect(key1 = invoiceId) {
        viewModel.fetchInvoiceLinesByInvoiceId(invoiceId)
        invoice = viewModel.getInvoiceById(invoiceId)
    }

    // null = loading, emptyList = loaded but no lines, populated = has lines
    val invoiceLinesState by viewModel.invoiceLines.collectAsState()
    val invoiceLines = invoiceLinesState ?: emptyList()
    val isLoadingLines = invoiceLinesState == null

    // Use the fetched lines directly for the header too — first line has the invoice metadata
    val invoiceLine = invoiceLines.firstOrNull()
    var isFabEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current

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
                if (invoiceLine != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Customer: ${invoiceLine.customerName} | Cashier: ${invoiceLine.cashier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${Date(invoiceLine.invoiceDate)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "#${invoiceLine.invoiceId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (isLoadingLines) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (invoiceLines.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(invoiceLines) { line ->
                            TheInvoiceCard(
                                invoiceLine = line,
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

        // Floating Action Button for printing / sharing PDF receipt
        FloatingActionButton(
            onClick = {
                scope.launch {
                    val bizInfo = viewModel.getBusinessInfo()
                    val inv = invoice ?: viewModel.getInvoiceById(invoiceId)
                    Utils.generateAndShareReceiptPdf(
                        context = context,
                        businessInfo = bizInfo,
                        invoice = inv,
                        invoiceLine = invoiceLine,
                        invoiceLines = invoiceLines
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Add padding from edges
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = "Print / Share Receipt",
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
        modifier = Modifier.padding(4.dp)
    ) {
        // Display brand name and shoe size
        Text(
            text = "($brandName) : Size - $sizeValue",
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
            text = " ${invoiceLine.productName} || Amount: $${invoiceLine.productPrice} || Sold: ${invoiceLine.productQtySold}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
