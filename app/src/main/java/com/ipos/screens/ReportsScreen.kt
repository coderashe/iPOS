package com.ipos.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipos.R
import com.ipos.app.utils.Utils
import com.ipos.data.Destinations
import com.ipos.data.entities.Invoice
import com.ipos.ui.theme.GreenColor
import com.ipos.viewmodels.InvoiceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

// ─── Report hub entry ─────────────────────────────────────────────────────────

private data class ReportEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: InvoiceViewModel = viewModel(),
    onInvoiceClick: (Long) -> Unit,
    navController: NavController
) {
    val reportCards = listOf(
        ReportEntry(
            title = "Sales",
            subtitle = "Invoices & transactions",
            icon = Icons.Default.Receipt,
            route = Destinations.SalesReport.route
        ),
        ReportEntry(
            title = "Multi-Currency",
            subtitle = "Sales by currency",
            icon = Icons.Default.CurrencyExchange,
            route = Destinations.MultiCurrency.route
        ),
        ReportEntry(
            title = "Stock Takes",
            subtitle = "History of stock counts",
            icon = Icons.Default.Inventory,
            route = Destinations.StockTakeReport.route
        ),
        ReportEntry(
            title = "Business Performance",
            subtitle = "Income & expenses",
            icon = Icons.Default.BarChart,
            route = Destinations.BusinessPerformance.route
        ),
        ReportEntry(
            title = "Expenses",
            subtitle = "Expense records",
            icon = Icons.Default.AttachMoney,
            route = Destinations.ExpenseReport.route
        ),
        ReportEntry(
            title = "Orders",
            subtitle = "Order history",
            icon = Icons.Default.Assessment,
            route = Destinations.OrderReports.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reports") })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(reportCards) { entry ->
                ReportHubCard(
                    entry = entry,
                    onClick = { navController.navigate(entry.route) }
                )
            }
        }
    }
}

@Composable
private fun ReportHubCard(entry: ReportEntry, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    entry.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Helpers (kept here — used by SalesReportScreen too) ─────────────────────

@Composable
fun ThreeDotMenu(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Today", "This Week", "This Month", "All")

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Filter")
        }
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
                    text = { Text(option) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun filterInvoicesByDate(invoices: List<Invoice>, filterOption: String): List<Invoice> {
    val currentDate = LocalDate.now()
    fun Long.toLocalDate() =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    return when (filterOption) {
        "Today" -> invoices.filter { it.invoiceDate.toLocalDate() == currentDate }
        "This Week" -> invoices.filter {
            it.invoiceDate.toLocalDate().isAfter(currentDate.minusWeeks(1))
        }
        "This Month" -> invoices.filter {
            it.invoiceDate.toLocalDate().month == currentDate.month
        }
        "All" -> invoices
        else -> invoices
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Invoice #${invoice.invoiceId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$${"%.2f".format(invoice.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                Date(invoice.invoiceDate).toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
