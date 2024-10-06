package com.example.learncompose303.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.viewmodels.InvoiceViewModel
import java.util.Date

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoiceViewModel = viewModel(),
) {

    // Trigger fetching invoiceLines based on the invoiceId
    LaunchedEffect(key1 = invoiceId) {
        viewModel.fetchAllInvoiceLines()
        viewModel.fetchInvoiceLinesByInvoiceId(invoiceId)
    }

    val theLines = viewModel.fetchAllInvoiceLines()
    Log.e("istartlogs", "invoicelines are ***** 2: $theLines")


    // Collect the filtered invoiceLines
    val invoiceLines by viewModel.invoiceLines.collectAsState()
    Log.e("istartlogs", "checking ***** 2: $invoiceLines")
    val invoiceLine = viewModel.getInvoiceLineById(invoiceId)
    Log.e("istartlogs", "checking ***** 1: $invoiceLine")

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
            }
            Spacer(modifier = Modifier.height(4.dp))

            if (invoiceLines.isNotEmpty()) {
                // Display the invoice lines using LazyColumn
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoiceLines) { invoiceLine ->
                        TheInvoiceCard(invoiceLine = invoiceLine)
                    }
                }
            } else {
                Text(text = "No items found for this invoice.")
            }
        }
    }
}

@Composable
fun TheInvoiceCard(
    invoiceLine: InvoiceLine,
) {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = invoiceLine.productName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
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
