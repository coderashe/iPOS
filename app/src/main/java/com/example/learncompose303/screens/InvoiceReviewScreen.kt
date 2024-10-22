package com.example.learncompose303.screens


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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceItem
import kotlinx.coroutines.flow.Flow


@Composable
fun InvoiceReviewScreen(
    invoiceItems: Flow<List<InvoiceItem>>,
    customer: Customer,
    onInvoiceSubmit: () -> Unit
) {
    // Collect the Flow<List<InvoiceItem>> and use State to store the items
    val collectedInvoiceItems by invoiceItems.collectAsState(initial = emptyList())

    // Calculate the total amount dynamically from the collected list
    val totalAmount = collectedInvoiceItems.sumOf { it.total }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Invoice Review", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Customer: ${customer.name}", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Display the invoice items using LazyColumn
        LazyColumn {
            items(collectedInvoiceItems) { item ->
                InvoiceItemCard(item = item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Amount: \$${totalAmount}", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onInvoiceSubmit,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Submit Invoice")
        }
    }
}

@Composable
fun InvoiceItemCard(item: InvoiceItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Item ID: ${item.productId}, Qty: ${item.quantity}")
            Text("Total: \$${item.total}")
        }
    }
}
