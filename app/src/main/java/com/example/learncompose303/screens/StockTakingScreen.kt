package com.example.learncompose303.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun StockTakingScreen(
    viewModel: InvoiceViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
) {
    val products by viewModel.productList.collectAsState()
    val context = LocalContext.current

    val stockTakes = remember { mutableStateListOf<StockTake>() }
    val loggedInUser by authViewModel.userEmail.observeAsState()
    val checkedItems =
        remember { mutableStateMapOf<Long, Boolean>() } // Track checked state per product


    // Trigger fetch when the email is available
    loggedInUser?.let {
        LaunchedEffect(it) {
            authViewModel.fetchUserByEmail(it)
        }
        Log.d("StartLogs", "current loggedin user is $loggedInUser")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(products) { product ->
            var countedStock by remember { mutableStateOf("") }
            val recordedStock = product.productQty
            val variance = countedStock.toDoubleOrNull()?.minus(recordedStock) ?: 0.0
            var varianceReason by remember { mutableStateOf("") }

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.productDescription,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Recorded Stock: $recordedStock", color = Color.Gray)

                    OutlinedTextField(
                        value = countedStock,
                        onValueChange = { countedStock = it },
                        label = { Text("Counted Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (variance != 0.0) {
                        Text(
                            "Variance: $variance",
                            color = if (variance < 0) Color.Red else Color.Green
                        )

                        OutlinedTextField(
                            value = varianceReason,
                            onValueChange = { varianceReason = it },
                            label = { Text("Reason for Variance") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            if (countedStock.isNotEmpty()) {
                                val stockTake = StockTake(
                                    productId = product.productId,
                                    recordedStock = recordedStock,
                                    countedStock = countedStock.toDouble(),
                                    variance = variance,
                                    varianceReason = varianceReason,
                                    dateRecorded = System.currentTimeMillis(),
                                    recordedBy = loggedInUser ?: "DEFAULT"
                                )

                                val existingIndex =
                                    stockTakes.indexOfFirst { it.productId == product.productId }
                                if (existingIndex != -1) {
                                    stockTakes[existingIndex] = stockTake // Update existing entry
                                } else {
                                    stockTakes.add(stockTake) // Add new entry
                                }

                                checkedItems[product.productId] =
                                    true // Mark only this product as checked
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = countedStock.isNotEmpty() && (checkedItems[product.productId] != true) // Disable only for this product
                    ) {
                        Text(if (checkedItems[product.productId] == true) "Checked" else "Check") // Change button text only for this product
                    }

                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = {
            stockTakes.forEach { viewModel.insertStockTake(it) }
            Toast.makeText(context, "Stock recorded!", Toast.LENGTH_SHORT).show()
            navController.navigate(Destinations.Dashboard.route) {
                popUpTo(Destinations.Dashboard.route) {
                    inclusive = true
                }
            }
        }
    ) {
        Text("CONFIRM")
    }
}
