package com.example.learncompose303.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.learncompose303.data.domain.ProductWithBrand
import com.example.learncompose303.data.entities.StockTakeSessionStatus
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.viewmodels.StockTakeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTakingScreen(
    stockTakeViewModel: StockTakeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val uiState by stockTakeViewModel.uiState.collectAsState()
    val loggedInUser by authViewModel.userEmail.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            stockTakeViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Stock Take", style = MaterialTheme.typography.titleMedium)
                        uiState.openSession?.let { session ->
                            val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            Text(
                                "Started ${fmt.format(Date(session.startedAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.openSession != null) {
                        TextButton(
                            onClick = { stockTakeViewModel.closeSession() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Close Session", fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.openSession != null && uiState.searchQuery.isNotBlank() &&
                uiState.filteredProducts.isEmpty()
            ) {
                // Search returned no results — offer to add a new product
                FloatingActionButton(
                    onClick = { stockTakeViewModel.showAddProductDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add new product")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.openSession == null -> {
                // No open session — show start screen
                NoOpenSessionScreen(
                    onStart = { loggedInUser?.let { stockTakeViewModel.startSession(it) } }
                )
            }

            else -> {
                // Active session
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Progress bar
                    SessionProgressHeader(
                        checked = uiState.checkedCount,
                        total = uiState.totalCount
                    )

                    // Search bar
                    StockTakeSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { stockTakeViewModel.onSearchQueryChanged(it) },
                        onAddNew = { stockTakeViewModel.showAddProductDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    if (uiState.filteredProducts.isEmpty() && uiState.searchQuery.isNotBlank()) {
                        EmptySearchResult(
                            query = uiState.searchQuery,
                            onAddNew = { stockTakeViewModel.showAddProductDialog() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp, vertical = 4.dp
                            )
                        ) {
                            items(
                                items = uiState.filteredProducts,
                                key = { it.product.productId }
                            ) { pwb ->
                                StockTakeItemRow(
                                    productWithBrand = pwb,
                                    isChecked = pwb.product.productId in uiState.countedProductIds,
                                    onRecord = { counted, reason ->
                                        stockTakeViewModel.recordItem(
                                            product = pwb.product,
                                            countedStock = counted,
                                            varianceReason = reason,
                                            recordedBy = loggedInUser ?: "unknown"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add new product dialog
    if (uiState.showAddProductDialog) {
        AddNewProductDialog(
            initialBrandName = uiState.searchQuery,
            onDismiss = { stockTakeViewModel.dismissAddProductDialog() },
            onConfirm = { brandName, desc, avgCost, stdCost, qty ->
                stockTakeViewModel.addNewBrandWithProductsAndRecord(
                    brandName = brandName,
                    productDescription = desc,
                    averageCost = avgCost,
                    standardCost = stdCost,
                    countedQtyPerSize = qty,
                    recordedBy = loggedInUser ?: "unknown"
                )
            }
        )
    }
}

// ─── No session screen ────────────────────────────────────────────────────────

@Composable
private fun NoOpenSessionScreen(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                "No active stock take",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Start a session to begin counting. The session stays open until you close it — " +
                        "you can leave and come back at any time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Stock Take")
            }
        }
    }
}

// ─── Progress header ──────────────────────────────────────────────────────────

@Composable
private fun SessionProgressHeader(checked: Int, total: Int) {
    val progress = if (total > 0) checked.toFloat() / total else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$checked / $total items",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
private fun StockTakeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search by name or code…") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
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
}

// ─── Empty search result ──────────────────────────────────────────────────────

@Composable
private fun EmptySearchResult(query: String, onAddNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "No product found for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(onClick = onAddNew) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Add \"$query\" as new product")
        }
    }
}

// ─── Compact item row ─────────────────────────────────────────────────────────

@Composable
private fun StockTakeItemRow(
    productWithBrand: ProductWithBrand,
    isChecked: Boolean,
    onRecord: (countedStock: Double, reason: String) -> Unit
) {
    val product = productWithBrand.product
    val brandName = productWithBrand.brand.brandName
    var expanded by rememberSaveable(product.productId) { mutableStateOf(!isChecked) }
    var countedStockText by rememberSaveable(product.productId) { mutableStateOf("") }
    var varianceReason by rememberSaveable(product.productId) { mutableStateOf("") }

    val variance = countedStockText.toDoubleOrNull()?.minus(product.productQty) ?: 0.0
    val hasVariance = countedStockText.isNotEmpty() && variance != 0.0

    val indicatorColor = when {
        isChecked && variance == 0.0 -> Color(0xFF4CAF50)   // green — matched
        isChecked && variance != 0.0 -> Color(0xFFFFA000)   // amber — variance
        else -> MaterialTheme.colorScheme.outlineVariant      // grey — not counted
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // ── Collapsed summary row ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Colour dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Spacer(Modifier.width(10.dp))

                // Product info
                Column(Modifier.weight(1f)) {
                    Text(
                        "$brandName - ${product.productDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Sys: ${product.productQty}  •  ${product.productCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (isChecked) {
                    // Show checked badge; tap to re-open for correction
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Checked — tap to edit",
                            tint = indicatorColor
                        )
                    }
                } else {
                    // Inline count field + check button
                    OutlinedTextField(
                        value = countedStockText,
                        onValueChange = { countedStockText = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier = Modifier.width(80.dp),
                        label = { Text("Count", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (countedStockText.isNotEmpty() && !hasVariance) {
                                onRecord(countedStockText.toDouble(), "")
                            }
                        }),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    FilledTonalButton(
                        onClick = {
                            if (countedStockText.isNotEmpty()) {
                                if (hasVariance) {
                                    expanded = true // show reason field
                                } else {
                                    onRecord(countedStockText.toDouble(), "")
                                }
                            }
                        },
                        enabled = countedStockText.isNotEmpty(),
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp
                        )
                    ) {
                        Text("Check", fontSize = 12.sp)
                    }
                }
            }

            // ── Variance reason (expandable) ───────────────────────────────
            AnimatedVisibility(
                visible = (hasVariance || isChecked) && expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(Modifier.padding(top = 8.dp)) {
                    if (hasVariance || (isChecked)) {
                        Text(
                            text = if (variance < 0) "Shortage: ${variance.toInt()}" else "Surplus: +${variance.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (variance < 0) MaterialTheme.colorScheme.error
                            else Color(0xFF2E7D32),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    OutlinedTextField(
                        value = varianceReason,
                        onValueChange = { varianceReason = it },
                        label = { Text("Reason for variance") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (countedStockText.isNotEmpty()) {
                                onRecord(countedStockText.toDouble(), varianceReason)
                            }
                        })
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            onRecord(
                                countedStockText.toDoubleOrNull() ?: product.productQty.toDouble(),
                                varianceReason
                            )
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isChecked) "Update" else "Check")
                    }
                }
            }
        }
    }
}

// ─── Add new product dialog (stepped) ────────────────────────────────────────

/**
 * Two-step dialog for adding a complete shoe brand with products.
 *
 * Step 1 — Brand name
 *   Enter or confirm the brand name. On "Next", the VM will resolve (or create) it.
 *
 * Step 2 — Product details
 *   Product description, avg cost, std cost, counted qty per size.
 *   On "Save & Record", triggers addNewBrandWithProductsAndRecord which creates
 *   9 ShoeSize entries (sizes 3-11, qty=100), 9 Products, 9 PriceList entries,
 *   and records all 9 in the current stock take session.
 */
@Composable
private fun AddNewProductDialog(
    initialBrandName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        brandName: String,
        description: String,
        averageCost: Double,
        standardCost: Double,
        countedQtyPerSize: Double
    ) -> Unit
) {
    // ── State ─────────────────────────────────────────────────────────────────
    var step by rememberSaveable { mutableStateOf(1) }  // 1 = brand, 2 = details

    // Step 1
    var brandName by rememberSaveable { mutableStateOf(initialBrandName) }

    // Step 2
    var description by rememberSaveable { mutableStateOf("") }
    var averageCostText by rememberSaveable { mutableStateOf("") }
    var standardCostText by rememberSaveable { mutableStateOf("") }
    var countedQtyText by rememberSaveable { mutableStateOf("100") }

    val descriptionFocusRequester = remember { FocusRequester() }

    // ── Validation ────────────────────────────────────────────────────────────
    val step1Valid = brandName.isNotBlank()
    val step2Valid = description.isNotBlank() &&
            averageCostText.toDoubleOrNull() != null &&
            standardCostText.toDoubleOrNull() != null &&
            countedQtyText.toDoubleOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ── Header ────────────────────────────────────────────────────
                Text(
                    text = "Add New Brand & Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "This will create 9 products (sizes 3–11) with 100 units each.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // ── Step indicator ────────────────────────────────────────────
                StepIndicator(currentStep = step, totalSteps = 2)

                Spacer(Modifier.height(20.dp))

                // ── Step content ──────────────────────────────────────────────
                if (step == 1) {
                    // Step 1: Brand name
                    Text(
                        "Step 1: Brand Name",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Brand Name *") },
                        placeholder = { Text("e.g. Nike, Adidas, Puma") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { if (step1Valid) step = 2 }
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "If this brand already exists, it will be reused.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Step 2: Product details
                    Text(
                        "Step 2: Product Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Brand: ${brandName.trim()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    // Trigger keyboard focus on description when step 2 appears
                    LaunchedEffect(Unit) {
                        descriptionFocusRequester.requestFocus()
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description *") },
                        placeholder = { Text("e.g. Air Force 1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(descriptionFocusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = averageCostText,
                            onValueChange = { averageCostText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Avg Cost *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = standardCostText,
                            onValueChange = { standardCostText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Std Cost *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = countedQtyText,
                        onValueChange = { countedQtyText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Counted Qty per Size *") },
                        placeholder = { Text("Default: 100") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (step2Valid) {
                                    onConfirm(
                                        brandName.trim(),
                                        description.trim(),
                                        averageCostText.toDouble(),
                                        standardCostText.toDouble(),
                                        countedQtyText.toDouble()
                                    )
                                }
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        supportingText = {
                            Text("9 products will be created (sizes 3–11), each recorded with this quantity.")
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        if (step == 1) onDismiss() else step = 1
                    }) {
                        Text(if (step == 1) "Cancel" else "Back")
                    }

                    if (step == 1) {
                        Button(
                            onClick = { step = 2 },
                            enabled = step1Valid
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                onConfirm(
                                    brandName.trim(),
                                    description.trim(),
                                    averageCostText.toDoubleOrNull() ?: 0.0,
                                    standardCostText.toDoubleOrNull() ?: 0.0,
                                    countedQtyText.toDoubleOrNull() ?: 100.0
                                )
                            },
                            enabled = step2Valid
                        ) {
                            Text("Save & Record")
                        }
                    }
                }
            }
        }
    }
}

// ─── Step indicator ───────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep

            val dotColor = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            Box(
                modifier = Modifier
                    .size(if (isActive) 28.dp else 24.dp)
                    .clip(CircleShape)
                    .background(if (isActive || isCompleted) dotColor else dotColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive || isCompleted)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < totalSteps - 1) {
                Divider(
                    modifier = Modifier.width(32.dp),
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
