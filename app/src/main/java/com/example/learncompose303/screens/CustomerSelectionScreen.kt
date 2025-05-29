package com.example.learncompose303.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.learncompose303.data.Destinations
import com.example.learncompose303.viewmodels.InvoiceViewModel
import com.example.learncompose303.data.entities.Customer

import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.EscPosPrinterCommands
import com.example.learncompose303.R
import com.example.learncompose303.app.utils.BluetoothPrinterScanner


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CustomerSelectionScreen(
    onCustomerSelected: (Customer) -> Unit,
    onNewCustomerClicked: () -> Unit,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.getWalkinCustomer()
    }
    // Fetch the customer list from the ViewModel
    val customers by viewModel.customerList.collectAsState()

    val walkInCustomer = viewModel.getWalkinCustomer()
    val context = LocalContext.current

    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(onClick = onNewCustomerClicked) {
//                Icon(Icons.Default.Add, contentDescription = "Add New Customer")
//            }
//        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row {


                    Text(
                        modifier = Modifier
                            .weight(1f),
                        text = stringResource(R.string.select_customer),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = {
//                            viewModel.selectedProducts
                            if (walkInCustomer != null) {
                                onCustomerSelected(walkInCustomer)
                            }
                        },
                    ) {
                        Text(text = "Skip")
                    }
                }
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
            Text(customer.email ?: "***")
            Text(customer.phone ?: "+263")
        }
    }
}
