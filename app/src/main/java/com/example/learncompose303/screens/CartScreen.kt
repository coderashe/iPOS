package com.example.learncompose303.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learncompose303.app.utils.BluetoothPrinterScanner
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.screens.auth.AuthState
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

// Walk-in customer used as default when no customer is selected
private val WALK_IN = Customer(customerId = 0, name = "Walk-in", phone = "", email = "", address = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: InvoiceViewModel,
    authViewModel: AuthViewModel,
    // Keep for back-compat but default to walk-in
    customer: Customer = WALK_IN
) {
    val context = LocalContext.current

    // selectedQuantities is the source of truth — it's populated by setProductQuantity
    // regardless of whether _selectedProducts is in sync.
    val selectedQuantities by viewModel.selectedQuantities.collectAsState()
    val withBrandProducts by viewModel.withBrandProductList.collectAsState()

    // Derive the product list directly from selectedQuantities + the product catalog.
    // This avoids dependency on _selectedProducts which can be stale/empty.
    val selectedProducts = remember(selectedQuantities, withBrandProducts) {
        withBrandProducts
            .filter { (selectedQuantities[it.product.productId] ?: 0) > 0 }
            .map { it.product }
    }

    val productWithPriceDetails by viewModel.productsWithPriceDetails.collectAsState(initial = emptyList())

    LaunchedEffect(selectedQuantities, withBrandProducts) {
        // Pass the derived selectedProducts (from quantities) so price details are always correct
        viewModel.getPriceDetailsForSelectedProducts(selectedProducts, selectedQuantities)
    }

    val currencies by viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    var currencyExpanded by remember { mutableStateOf(false) }
    var currencyLabel by remember { mutableStateOf("United States Dollar") }

    var amountTendered by remember { mutableStateOf("") }

    val baseTotalPrice by remember {
        derivedStateOf {
            productWithPriceDetails.sumOf { (product, price) ->
                val qty = selectedQuantities[product.productId] ?: 1
                price * qty
            }
        }
    }
    val exchangeRate = selectedCurrency?.rateToBase ?: 1.0
    val convertedTotal = baseTotalPrice * exchangeRate
    val changeBalance = amountTendered.toDoubleOrNull()?.let { it - convertedTotal } ?: 0.0

    val cashierEmail = authViewModel.userEmail.value ?: "unknown"
    val authState by authViewModel.authState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Order summary card ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    selectedProducts.forEach { product ->
                        val price = productWithPriceDetails
                            .find { it.first.productId == product.productId }?.second ?: 0.0
                        val qty = selectedQuantities[product.productId] ?: 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${product.productDescription} x$qty",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${"%.2f".format(price * qty)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(
                            "${selectedCurrency?.code ?: "$"} ${"%.2f".format(convertedTotal)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Currency selector ────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currencyLabel,
                    onValueChange = {},
                    label = { Text(selectedCurrency?.name ?: "Currency") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currencyExpanded = true },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { currencyExpanded = !currencyExpanded }
                        )
                    }
                )
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency.name) },
                            onClick = {
                                currencyLabel = currency.name
                                viewModel.selectCurrency(currency)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Amount tendered ──────────────────────────────────────────
            OutlinedTextField(
                value = amountTendered,
                onValueChange = { amountTendered = it },
                label = { Text("Amount Tendered (${selectedCurrency?.code ?: "$"})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // ── Change ───────────────────────────────────────────────────
            if (amountTendered.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${selectedCurrency?.code ?: "$"} ${"%.2f".format(changeBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (changeBalance >= 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── PAY button ───────────────────────────────────────────────
            Button(
                onClick = {
                    val tendered = amountTendered.toDoubleOrNull()
                    when {
                        tendered == null -> Toast.makeText(
                            context, "Enter amount tendered", Toast.LENGTH_SHORT
                        ).show()
                        tendered < convertedTotal -> Toast.makeText(
                            context,
                            "Amount tendered must be ≥ total",
                            Toast.LENGTH_SHORT
                        ).show()
                        authState != AuthState.Authenticated -> {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Destinations.SignIn.route) {
                                popUpTo(Destinations.SignIn.route) { inclusive = true }
                            }
                        }
                        else -> {
                            // Capture all cart data as local vals RIGHT NOW (on the Main thread,
                            // synchronously) before clearCart() wipes ViewModel state.
                            val productsSnapshot = selectedProducts.toList()
                            val quantitiesSnapshot = selectedQuantities.toMap()
                            // Build productId → converted price map from already-collected UI state
                            val pricesSnapshot = productWithPriceDetails.associate { (product, basePrice) ->
                                product.productId to (basePrice * exchangeRate)
                            }
                            val currencyCodeSnapshot = selectedCurrency?.code ?: "USD"

                            viewModel.submitInvoice(
                                customerId        = customer.customerId,
                                amountTendered    = tendered,
                                products          = productsSnapshot,
                                productQuantities = quantitiesSnapshot,
                                productPrices     = pricesSnapshot,
                                currencyCode      = currencyCodeSnapshot,
                                cashierEmail      = cashierEmail
                            )

                            val productDetails = productsSnapshot.joinToString("\n") { p ->
                                val qty = quantitiesSnapshot[p.productId] ?: 1
                                val price = pricesSnapshot[p.productId] ?: 0.0
                                "${p.productDescription} :*$qty :@ ${"%.2f".format(price)}"
                            }
                            val message = "RECEIPT: ${Date(System.currentTimeMillis())}\n\n" +
                                "CUSTOMER: ${customer.name}\n\n" +
                                "PRODUCTS\n$productDetails\n\n" +
                                "Total:    ${selectedCurrency?.code ?: "$"} ${"%.2f".format(convertedTotal)}\n" +
                                "Tendered: ${selectedCurrency?.code ?: "$"} ${"%.2f".format(tendered)}\n" +
                                "Change:   ${selectedCurrency?.code ?: "$"} ${"%.2f".format(changeBalance)}\n\n" +
                                "THANK YOU!"

                            BluetoothPrinterScanner(context).connectToHardcodedPrinter(message)

                            // All data is already captured in local snapshots passed to submitInvoice.
                            // Safe to clear cart now — no race condition possible.
                            viewModel.clearCart()

                            navController.navigate(Destinations.ProductSelection.route) {
                                popUpTo(Destinations.ProductSelection.route) { inclusive = true }
                            }
                            Toast.makeText(context, "Payment successful", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = amountTendered.isNotBlank()
            ) {
                Text("PAY  ${selectedCurrency?.code ?: "$"} ${"%.2f".format(convertedTotal)}",
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
