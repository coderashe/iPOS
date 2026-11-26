package com.ipos.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ipos.app.AddBrandWithProductsDialog
import com.ipos.app.AddSingleProductDialog
import com.ipos.app.core.BusinessType
import com.ipos.data.entities.Customer
import com.ipos.viewmodels.InvoiceViewModel
import kotlinx.coroutines.launch

// Walk-in default customer — no customer selection screen needed
private val WALK_IN_CUSTOMER = Customer(
    customerId = 0,
    name = "Walk-in",
    phone = "",
    email = "",
    address = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectionScreen(
    onProceedToCart: () -> Unit,
    navController: NavController,
    viewModel: InvoiceViewModel,
    // Keep customer param for back-compat but default to walk-in
    customer: Customer = WALK_IN_CUSTOMER
) {
    val withBrandProducts by viewModel.withBrandProductList.collectAsState()
    val selectedProducts by viewModel.selectedProducts.collectAsState()
    val selectedQuantities by viewModel.selectedQuantities.collectAsState()
    val businessType by viewModel.businessType.collectAsState()

    // No local qty map — quantities live in the ViewModel

    var searchQuery by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Always start fresh — cart was cleared by CartScreen before navigating back,
        // or this is a new entry from Dashboard/drawer. No flag needed.
        viewModel.clearCart()
        viewModel.fetchProducts()
        viewModel.fetchBrandProducts()
    }

    val query = searchQuery.trim().lowercase()
    val filteredProducts = withBrandProducts.filter {
        query.isBlank() ||
        it.product.productDescription.lowercase().contains(query) ||
        (it.brand?.brandName ?: "").lowercase().contains(query) ||
        it.product.productCode.lowercase().contains(query)
    }

    // Running totals for the bottom bar
    val totalItems = selectedQuantities.values.sum()
    val totalPrice = withBrandProducts.sumOf { pwb ->
        val qty = selectedQuantities[pwb.product.productId] ?: 0
        val price = viewModel.getPricelistById(pwb.product.productId)?.sellingPrice ?: 0.0
        price * qty
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Products") },
                actions = {
                    if (totalItems > 0) {
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.clearCart() }
                        ) {
                            Text(
                                "New Sale",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (totalItems > 0) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "$totalItems item${if (totalItems != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Total: ${"%.2f".format(totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = {
                                // Quantities are already live in viewModel.selectedQuantities.
                                // CartScreen collects them directly — no manual sync needed.
                                onProceedToCart()
                            }
                        ) {
                            Text("Proceed to Cart")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Search bar ───────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search by name, brand or code…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Search
                )
            )

            // ── Empty search state ───────────────────────────────────────
            if (filteredProducts.isEmpty() && query.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "No product found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val btnText = if (businessType == BusinessType.SHOES) {
                        "Add \"$searchQuery\" as new brand"
                    } else {
                        "Add \"$searchQuery\" as new product"
                    }
                    FilledTonalButton(onClick = { showAddProductDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(btnText)
                    }
                }
            } else {
                // ── Product list ─────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = filteredProducts,
                        key = { it.product.productId }
                    ) { pwb ->
                        val price = viewModel.getPricelistById(pwb.product.productId)?.sellingPrice ?: 0.0
                        val qty = selectedQuantities[pwb.product.productId] ?: 0
                        ProductSelectionRow(
                            name = pwb.product.productDescription,
                            brand = pwb.brand?.brandName ?: (pwb.product.category ?: ""),
                            code = pwb.product.productCode,
                            price = price,
                            stock = pwb.product.productQty,
                            qty = qty,
                            onQtyChange = { newQty ->
                                if (newQty <= pwb.product.productQty) {
                                    viewModel.setProductQuantity(pwb.product.productId, newQty)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Only ${pwb.product.productQty} in stock",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Add product dialog (Shoe brand+sizes OR Single product) ─────────────
    if (showAddProductDialog) {
        if (businessType == BusinessType.SHOES) {
            AddBrandWithProductsDialog(
                initialBrandName = searchQuery,
                onDismiss = { showAddProductDialog = false },
                onConfirm = { brandName, desc, avgCost, stdCost, qty ->
                    viewModel.addNewBrandWithProducts(
                        brandName = brandName,
                        productDescription = desc,
                        averageCost = avgCost,
                        standardCost = stdCost,
                        qtyPerSize = qty,
                        onComplete = { message, _ ->
                            showAddProductDialog = false
                            searchQuery = ""
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    )
                }
            )
        } else {
            AddSingleProductDialog(
                initialName = searchQuery,
                businessType = businessType,
                onDismiss = { showAddProductDialog = false },
                onConfirm = { name, category, unit, sellingPrice, unitCost, qty ->
                    viewModel.addNewSingleProduct(
                        productName = name,
                        category = category,
                        unit = unit,
                        sellingPrice = sellingPrice,
                        unitCost = unitCost,
                        stockQty = qty,
                        onComplete = { message, _ ->
                            showAddProductDialog = false
                            searchQuery = ""
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    )
                }
            )
        }
    }
}

// ─── Compact product row (StockTake style) ────────────────────────────────────

@Composable
private fun ProductSelectionRow(
    name: String,
    brand: String,
    code: String,
    price: Double,
    stock: Long,
    qty: Int,
    onQtyChange: (Int) -> Unit
) {
    val isOutOfStock = stock <= 0

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                qty > 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                isOutOfStock -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Product info ─────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$brand – $name",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.2f".format(price)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isOutOfStock) "Out of stock" else "Left: $stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutOfStock) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // ── Qty controls ─────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { if (qty > 0) onQtyChange(qty - 1) },
                    modifier = Modifier.size(32.dp),
                    enabled = qty > 0
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = qty.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                IconButton(
                    onClick = { if (!isOutOfStock) onQtyChange(qty + 1) },
                    modifier = Modifier.size(32.dp),
                    enabled = !isOutOfStock && qty < stock
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
