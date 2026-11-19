package com.ipos.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ipos.app.utils.Utils
import com.ipos.viewmodels.InvoiceViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesReportScreen(
    viewModel: InvoiceViewModel,
    navController: NavController,
    onInvoiceClick: (Long) -> Unit
) {
    var filterOption by remember { mutableStateOf("Today") }
    val invoices by viewModel.invoiceList.collectAsState()
    val filteredInvoices = filterInvoicesByDate(invoices, filterOption)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filteredCurrencyTotals = filteredInvoices
        .groupBy { it.currencyCode.ifBlank { "USD" } }
        .mapValues { e -> e.value.sumOf { it.totalAmount } }

    LaunchedEffect(Unit) { viewModel.fetchAllInvoices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ThreeDotMenu(
                        selectedOption = filterOption,
                        onOptionSelected = { filterOption = it }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    val bizInfo = viewModel.getBusinessInfo()
                    Utils.generateAndShareSalesReportPdf(
                        context = context,
                        storeName = bizInfo.storeName,
                        filterTitle = filterOption,
                        invoices = filteredInvoices,
                        currencyTotals = filteredCurrencyTotals
                    )
                }
            }) {
                Icon(Icons.Default.Print, contentDescription = "Print / Share Sales Report")
            }
        }
    ) { padding ->
        if (filteredInvoices.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                Text(
                    "No invoices for $filterOption",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                items(filteredInvoices) { invoice ->
                    InvoiceCard(invoice = invoice, onClick = { onInvoiceClick(invoice.invoiceId) })
                }
            }
        }
    }
}
