package com.example.learncompose303.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.app.utils.Utils
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.viewmodels.InvoiceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Composable
fun ReportsScreen(
    viewModel: InvoiceViewModel = viewModel(),
    onInvoiceClick: (Long) -> Unit,
    navController: NavController
) {
    // State for filter option
    var filterOption by remember { mutableStateOf("Today") }

    // Collect invoices from the ViewModel
    val invoices by viewModel.invoiceList.collectAsState()

    // Filter invoices based on the selected filter option
    val filteredInvoices = filterInvoicesByDate(invoices, filterOption)

    val context = LocalContext.current
    val orders by viewModel.orderList.collectAsState()

    // Build the order details string
    val orderDetails = orders.joinToString("\n") { order ->
        "Order: ${order.productName}   Qty: ${order.quantity} \n Notes: ${order.notes} \n Date: ${
            Date(order.orderDate)
        }"
    }

    val invoiceList by viewModel.invoiceList.collectAsState()
    val (oldSalesTotal, numberOfTransactions) = viewModel.getTodaySalesInfo()

    // Log raw invoice list
    Log.d("todays", "RAW INVOICE LIST: ${invoiceList.size} invoices")
    invoiceList.forEach { invoice ->
        Log.d(
            "todays",
            "Invoice ID: ${invoice.invoiceId}, Date: ${invoice.invoiceDate}, Amount: ${invoice.totalAmount}"
        )
    }

    // Get today's date
    val today = LocalDate.now()
    Log.d("todays", "TODAY'S DATE: $today")

    // Filter invoices for today only - FIXED VERSION
    val todayInvoices = invoiceList.filter { invoice ->
        val invoiceDate = Instant.ofEpochMilli(invoice.invoiceDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        println("Converted invoice date: $invoiceDate")
        invoiceDate.isEqual(today)
    }

    Log.d("todays", "TODAY'S INVOICES COUNT: ${todayInvoices.size}")

    // Group invoices by currency and sum totals - now using today's filtered list
    val currencyTotals = todayInvoices.groupBy { it.currencyCode }
        .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

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

            // New Shoe Size | New Shoe Brand Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        navController.navigate(Destinations.MultiCurrency.route) // Navigate to ShoeMgtScreen
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        stringResource(R.string.multi_currency),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        "Total Sales: $$currencyTotals\n" +
                        "Total Invoices: $numberOfTransactions\n\n" +
                        "*ORDERS:*\n" +
                        "$orderDetails\n\n" +
                        "THANK YOU!!!... powered by istartk \u00A92024"

                Utils.generateAndShareInvoice(message, context)
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
                text = "$${"%.2f".format(invoice.totalAmount)}",
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

//Multi-Currency Report – Shows sales in different currencies.
//Stock Value Report – Displays total stock value.
//Sales by Person Report – Lists sales per salesperson.
//Top Performing Salesman – Highlights the best-performing salesperson.
//Tax Revenue Report – Calculates 15% tax on sales.
//Daily Sales Report – Shows sales and transactions for today.
//Other relevant reports like Profit Report, Customer Purchase Trends, and Monthly Revenue Breakdown.