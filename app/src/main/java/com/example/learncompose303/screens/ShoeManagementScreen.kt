package com.example.learncompose303.screens

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.viewmodels.InvoiceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.learncompose303.app.utils.BluetoothPrinterScanner
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.viewmodels.ShoeViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@Composable
fun ShoeManagementScreen(shoeViewModel: ShoeViewModel, viewModel: InvoiceViewModel) {
    val context = LocalContext.current
    val shoeBrands by shoeViewModel.shoeBrands.collectAsState()

    var showShoeSizeDialog by remember { mutableStateOf(false) }
    var showShoeBrandDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // New Shoe Size Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { showShoeSizeDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("New Shoe Size", style = MaterialTheme.typography.titleMedium)
            }
        }

        // New Shoe Brand Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { showShoeBrandDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("New Shoe Brand", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showShoeSizeDialog) {
        ShoeSizeDialog(
            viewModel = viewModel,
            onDismiss = { showShoeSizeDialog = false }) { brandID, size, quantity ->
            shoeViewModel.insertShoeSize(brandID, size, quantity)
            showShoeSizeDialog = false
        }
    }

    if (showShoeBrandDialog) {
        ShoeBrandDialog(onDismiss = { showShoeBrandDialog = false }) { brandName ->
            shoeViewModel.insertShoeBrand(brandName)
            showShoeBrandDialog = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoeSizeDialog(
    viewModel: InvoiceViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Long, Double, Long) -> Unit
) {
    val context = LocalContext.current
    val shoeBrands by viewModel.shoeBrands.collectAsState()
    val shoeBrandList = shoeBrands.map { it.brandName }      // Extract only brand names
    var shoeBrand by remember { mutableStateOf("Nike") } // Default Brand -> "Nike"
    var brandExpanded by remember { mutableStateOf(false) }     // Dropdown Menu state
    var selectedShoeBrand by remember { mutableStateOf<ShoeBrand?>(null) }
    var size by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Shoe Size") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Shoe Brand Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Anchor for the dropdown
                    OutlinedTextField(
                        value = shoeBrand,
                        onValueChange = {},
                        label = { Text("Shoe Brand") },
                        readOnly = true,
                        modifier = Modifier
                            .clickable { brandExpanded = true },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    brandExpanded = !brandExpanded
                                } // Also toggle using the icon
                            )
                        }
                    )
                    // DropdownMenu should be anchored to the TextField
                    DropdownMenu(
                        expanded = brandExpanded,
                        onDismissRequest = { brandExpanded = false },
                    ) {
                        shoeBrandList.forEach { selectedBrand ->
                            androidx.compose.material.DropdownMenuItem(onClick = {
                                shoeBrand = selectedBrand
                                selectedShoeBrand =
                                    shoeBrands.find { it.brandName == selectedBrand }
                                brandExpanded = false
                            }) {
                                Text(text = selectedBrand)
                            }
                        }
                    }
                }

                // Size Input
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                // Quantity Input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedShoeBrand?.let { brand ->
                        onConfirm(brand.brandID, size.toDouble(), quantity.toLong())
                    }
                    Toast.makeText(context, "New Shoe Size Added Succesful!", Toast.LENGTH_SHORT)
                        .show()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ShoeBrandDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var brandName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Shoe Brand") },
        text = {
            OutlinedTextField(
                value = brandName,
                onValueChange = { brandName = it },
                label = { Text("Brand Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(brandName)
                    Toast.makeText(context, "New Shoe Size Added Succesful!", Toast.LENGTH_SHORT)
                        .show()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
