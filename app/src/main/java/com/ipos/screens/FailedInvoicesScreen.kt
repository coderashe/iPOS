package com.ipos.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ipos.data.entities.FailedInvoiceItem
import com.ipos.viewmodels.InvoiceViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedInvoicesScreen(
    viewModel: InvoiceViewModel,
    cashierEmail: String,
    onBack: () -> Unit
) {
    val failedItems by viewModel.failedInvoiceItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Failed Items (${failedItems.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (failedItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearFailedItems() }) {
                            Text("Clear All")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (failedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No failed items")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(failedItems, key = { it.rowIndex }) { item ->
                    FailedItemCard(
                        item = item,
                        viewModel = viewModel,
                        onRetry = { failedItem, callback -> 
                            viewModel.retryFailedItem(failedItem, cashierEmail, callback)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FailedItemCard(
    item: FailedInvoiceItem,
    viewModel: InvoiceViewModel,
    onRetry: (FailedInvoiceItem, (Boolean, String) -> Unit) -> Unit
) {
    var brand by remember(item.rowIndex) { mutableStateOf(item.brand) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var availableSizes by remember { mutableStateOf<List<Double>>(emptyList()) }
    var availableColors by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedSize by remember(item.rowIndex) { mutableStateOf(item.size) }
    var selectedColor by remember(item.rowIndex) { mutableStateOf(item.description) }
    
    var sizeExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(brand) {
        scope.launch {
            availableSizes = viewModel.getAvailableSizesForBrand(brand)
            availableColors = viewModel.getAvailableColorsForBrand(brand)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Row ${item.rowIndex}", style = MaterialTheme.typography.labelSmall)
            Text(item.failureReason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            
            if (!item.suggestedMatches.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Suggestions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.suggestedMatches.split(", ").forEach { suggestion ->
                        SuggestionChip(
                            onClick = { brand = suggestion; item.brand = suggestion },
                            label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it; item.brand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenuBox(
                expanded = sizeExpanded,
                onExpandedChange = { sizeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSize.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Size") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = sizeExpanded,
                    onDismissRequest = { sizeExpanded = false }
                ) {
                    availableSizes.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size.toString()) },
                            onClick = {
                                selectedSize = size
                                item.size = size
                                sizeExpanded = false
                            }
                        )
                    }
                }
            }
            
            ExposedDropdownMenuBox(
                expanded = colorExpanded,
                onExpandedChange = { colorExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedColor,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Color/Description") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = colorExpanded,
                    onDismissRequest = { colorExpanded = false }
                ) {
                    availableColors.forEach { color ->
                        DropdownMenuItem(
                            text = { Text(color) },
                            onClick = {
                                selectedColor = color
                                item.description = color
                                colorExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
                Text("Customer: ${item.customerId}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    onRetry(item) { success, message ->
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = availableSizes.isNotEmpty() && availableColors.isNotEmpty()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}
