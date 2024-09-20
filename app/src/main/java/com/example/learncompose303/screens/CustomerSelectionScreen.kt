package com.example.learncompose303.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.viewmodels.InvoiceViewModel
import com.example.learncompose303.data.entities.Customer

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomerSelectionScreen(
    onCustomerSelected: (Customer) -> Unit,
    onNewCustomerClicked: () -> Unit,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    // Fetch the customer list from the ViewModel
    val customers by viewModel.customerList.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNewCustomerClicked) {
                Icon(Icons.Default.Add, contentDescription = "Add New Customer")
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Select Customer", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Show the list of customers
                LazyColumn {
                    items(customers) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = {
                                onCustomerSelected(customer)
                            }
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(customer.name, fontWeight = FontWeight.Bold)
            Text(customer.email ?: "No email")
            Text(customer.phone ?: "No phone")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCustomerSelectionScreen() {
    val navController = rememberNavController()
    val customers = listOf(
        Customer(1, "John Doe", "john@example.com", "123456789", "State House"),
        Customer(2, "Jane Smith", "jane@example.com", "987654321", "State House")
    )

    // Sample preview of the screen
    CustomerSelectionScreen(
        onCustomerSelected = { /* Do nothing for preview */ },
        onNewCustomerClicked = { /* Do nothing for preview */ },
        navController
    )
}

