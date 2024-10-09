package com.example.learncompose303.screens

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.viewmodels.InvoiceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportsScreen(
    viewModel: InvoiceViewModel = viewModel(),
    onInvoiceClick: (Long) -> Unit,
) {
    // State for filter option
    var filterOption by remember { mutableStateOf("Today") }

    // Collect invoices from the ViewModel
    val invoices by viewModel.invoiceList.collectAsState()

    // Filter invoices based on the selected filter option
    val filteredInvoices = filterInvoicesByDate(invoices, filterOption)

    val context = LocalContext.current
    val (currentSalesTotal, numberOfTransactions) = viewModel.getTodaySalesInfo()
    val orders by viewModel.orderList.collectAsState()

    // Build the order details string
    val orderDetails = orders.joinToString("\n") { order ->
        "Order: ${order.productName}   Qty: ${order.quantity} \n Notes: ${order.notes} \n Date: ${
            Date(order.orderDate)
        }"
    }

    LaunchedEffect(key1 = true) {
        viewModel.fetchAllInvoices()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Dropdown for selecting the filter
            ThreeDotMenu(
                selectedOption = filterOption,
                onOptionSelected = {
                    filterOption = it // Update filterOption when a new option is selected
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display filtered invoices
            if (filteredInvoices.isEmpty()) {
                Text(
                    text = "No Invoices for $filterOption",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredInvoices) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.invoiceId) })
                    }
                }
            }

        }
        // Floating Action Button for sending the report
        FloatingActionButton(
            onClick = {
                val message = "*${Date(System.currentTimeMillis())} REPORT:*\n\n" +
                        "*INVOICES:*\n" +
                        "Total Sales: $$currentSalesTotal\n" +
                        "Total Invoices: $numberOfTransactions\n\n" +
                        "*ORDERS:*\n" +
                        "$orderDetails\n\n" +
                        "THANK YOU!!!... powered by istartk \u00A92024"

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }

                val chooserIntent =
                    Intent.createChooser(sendIntent, "Send Report via Messaging")
                context.startActivity(chooserIntent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
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
fun ThreeDotMenu(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Today", "This Week", "This Month", "All")

    Box {
        // Three-dot menu icon
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert, // Three-dot icon
                contentDescription = "Menu"
            )
        }

        // Popup menu for options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = {
                        Text(text = option) // Provide the text parameter correctly here
                    }
                )
            }
        }
    }
}


// Filtering logic for invoices based on the selected time period
@RequiresApi(Build.VERSION_CODES.O)
fun filterInvoicesByDate(invoices: List<Invoice>, filterOption: String): List<Invoice> {
    val currentDate = LocalDate.now()

    // Function to convert Long (Unix timestamp) to LocalDate
    fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    return when (filterOption) {
        "Today" -> invoices.filter { it.invoiceDate.toLocalDate() == currentDate }
        "This Week" -> invoices.filter {
            it.invoiceDate.toLocalDate().isAfter(currentDate.minusWeeks(1))
        }

        "This Month" -> invoices.filter { it.invoiceDate.toLocalDate().month == currentDate.month }
        "All" -> invoices
        else -> invoices
    }
}


//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun ReportsScreen(
//    viewModel: InvoiceViewModel = viewModel(),
//    onInvoiceClick: (Long) -> Unit,
//) {
//    val orders by viewModel.orderList.collectAsState()
//    val totalOrders = viewModel.getTotalOrdersForToday()
//    val invoices by viewModel.invoiceList.collectAsState()
//    val context = LocalContext.current
//    val (currentSalesTotal, numberOfTransactions) = viewModel.getTodaySalesInfo()
//    // Build the order details string
//    val orderDetails = orders.joinToString("\n") { order ->
//        "Order: ${order.productName}   Qty: ${order.quantity} \n Notes: ${order.notes} \n Date: ${
//            Date(
//                order.orderDate
//            )
//        }"
//    }
//
//    LaunchedEffect(key1 = true) {
//        viewModel.fetchAllInvoices()
//    }
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        if (invoices.isEmpty()) {
//            // Display message when there are no orders
//            Text(
//                text = "No Invoices yet!",
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .padding(16.dp),
//                style = MaterialTheme.typography.headlineSmall,
//                textAlign = TextAlign.Center,
//                color = MaterialTheme.colorScheme.error
//            )
//        } else {
//
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(8.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(invoices) { invoice ->
//                    InvoiceCard(invoice = invoice, onClick = {
//                        Log.e(
//                            "istartlogs",
//                            "Navigating with invoiceId: ${invoice.invoiceId}"
//                        ) // Log the invoiceId before navigation
//                        onInvoiceClick(invoice.invoiceId)  // Pass the correct invoiceId
//                    })
//                }
//            }
//        }
//
//        // Floating Action Button
//        FloatingActionButton(
//            onClick = {
//                // Code for sharing the invoice message...
//
//                val message = "*${Date(System.currentTimeMillis())} REPORT:*\n\n\n" +
//                        "*INVOICES:*\n" +
//                        "Total Sales: $$currentSalesTotal\n" +
//                        "Total Invoices: $numberOfTransactions\n\n" +
//                        "*ORDERS:*\n" +
//                        "Total Orders: $totalOrders\n" +
//                        "$orderDetails\n\n" +
//                        "THANK YOU!!!... powered by istartk \u00A92024"
//
//                val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                    type = "text/plain"
//                    putExtra(Intent.EXTRA_TEXT, message)
//                }
//
//                try {
//                    val chooserIntent = Intent.createChooser(
//                        sendIntent,
//                        "Send via WhatsApp or WhatsApp Business"
//                    )
//                    chooserIntent.resolveActivity(context.packageManager)?.let {
//                        context.startActivity(chooserIntent)
//                    } ?: run {
//                        Toast.makeText(
//                            context,
//                            "No messaging app found",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
//                        .show()
//                }
//
////
//
//            },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp) // Add padding from edges
//        ) {
//            Icon(
//                imageVector = Icons.Default.Send,
//                contentDescription = "Send Report",
//                tint = GreenColor
//            )
//        }
//    }
//}


@Composable
fun InvoiceCard(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
//        colors = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Invoice: ${invoice.invoiceId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${invoice.totalAmount}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${Date(invoice.invoiceDate)}", // Formatting date can be customized
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }
    }
}
