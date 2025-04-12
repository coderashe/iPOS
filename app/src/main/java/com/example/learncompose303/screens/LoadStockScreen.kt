package com.example.learncompose303.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.viewmodels.InvoiceViewModel
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize

@Composable
fun LoadStockScreen(
    viewModel: InvoiceViewModel,
    navController: NavController,
    onProductAdded: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var currentStockLevel by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    val context = LocalContext.current
    val shoeSizes by viewModel.shoeSizes.collectAsState()
    val shoeBrands by viewModel.shoeBrands.collectAsState()
    val shoeBrandList = shoeBrands.map { it.brandName }      // Extract only brand names
    val shoeSizeList = shoeSizes.map { it.size.toString() }  // Extract only sizes
    var selectedShoeSize by remember { mutableStateOf<ShoeSize?>(null) }
    var selectedShoeBrand by remember { mutableStateOf<ShoeBrand?>(null) }
    var shoeSize by remember { mutableStateOf("3.0") } // Default size -> 6
    var shoeBrand by remember { mutableStateOf("OLDSTOCK") } // Default Brand -> "Nike"
    var sizeExpanded by remember { mutableStateOf(false) }     // Dropdown Menu state
    var brandExpanded by remember { mutableStateOf(false) }     // Dropdown Menu state
    var showDialog by remember { mutableStateOf(false) } // Show confirmation dialog
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchShoeBrands()
        viewModel.fetchShoeSizes()
    }

    val requestFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let {
                viewModel.uploadProductsFromCsv(it, context)
            }
        }
    }

//

    // When showing the dialog, launch a coroutine to check for duplicates
    if (showDialog) {
        LaunchedEffect(productName, selectedShoeSize) {
            // Run the suspend function in a coroutine scope
            val product =
                viewModel.getProductByNameAndSize(productName, selectedShoeSize?.shoeSizeId ?: 0)
            if (product != null) {
                errorMessage =
                    "This shoe already exists in the inventory with the same name and size."
                showDialog = false
                Toast.makeText(context, "$errorMessage", Toast.LENGTH_SHORT).show()
            } else {
                errorMessage = null
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirm Adding Shoe") },
            text = {
                Column {
                    Text("Are you sure you want to add this shoe to the inventory?")
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = Color.Red)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle adding product logic
                        val unitCostValue = unitCost.toDoubleOrNull()
                        val sellingPriceValue = sellingPrice.toDoubleOrNull()
                        val currentStockLevelValue = currentStockLevel.toLongOrNull()

                        // Ensure brand is selected, otherwise pick the first available brand
                        if (selectedShoeBrand == null && shoeBrands.isNotEmpty()) {
                            selectedShoeBrand = shoeBrands.first()
                        }

                        if ((unitCostValue != null) &&
                            (currentStockLevelValue != null) &&
                            (selectedShoeSize != null) &&
                            (selectedShoeBrand != null)
                        ) {
                            val totalCost = unitCostValue * currentStockLevelValue
                            val newProducts = Products(
                                productDescription = productName,
                                saleable = true,
                                purchasable = true,
                                averageCost = totalCost,
                                standardCost = unitCostValue,
                                salesTaxCode = "VAT",
                                productCode = "ISTART32",
                                inclusiveTaxed = true,
                                shoeSizeId = selectedShoeSize!!.shoeSizeId,
                                brandID = selectedShoeBrand!!.brandID,
                                productQty = currentStockLevelValue
                            )
                            viewModel.insertTheProduct(newProducts) { productId ->
                                val newPriceList = sellingPriceValue?.let {
                                    PriceList(
                                        productId = productId,
                                        productDescription = productName,
                                        unitCost = unitCostValue,
                                        sellingPrice = it,
                                        markupPercentage = 0.0,
                                        grossProfitPercentage = 0.0
                                    )
                                }
                                if (newPriceList != null) {
                                    viewModel.insertPriceList(newPriceList)
                                }
                                onProductAdded()
                                Toast.makeText(
                                    context,
                                    "Product and Price added!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                productName = ""
                                unitCost = ""
                                currentStockLevel = ""
                                sellingPrice = ""
                                selectedShoeSize = null
                                selectedShoeBrand = null
                                navController.navigate(Destinations.Dashboard.route)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Enter all details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Close dialog after attempting to add
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.primary)
                ) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            },
            backgroundColor = Color.White,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }

//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
        ) {
            // New Shoe Size | New Shoe Brand Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        navController.navigate(Destinations.ShoeManagement.route) // Navigate to ShoeMgtScreen
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        stringResource(R.string.shoe_mgt),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Upload Products From Template
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        requestFileLauncher.launch("*/*")
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        stringResource(R.string.uploadProducts),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

//
        Spacer(modifier = Modifier.height(12.dp))
//        dropdowns section
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shoe Size Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Anchor for the dropdown
                OutlinedTextField(
                    value = shoeSize,
                    onValueChange = {},
                    label = { Text("Shoe Size") },
                    readOnly = true,
                    modifier = Modifier
                        .clickable { sizeExpanded = true },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                sizeExpanded = !sizeExpanded
                            } // Also toggle using the icon
                        )
                    }
                )

                // DropdownMenu should be anchored to the TextField
                DropdownMenu(
                    expanded = sizeExpanded,
                    onDismissRequest = { sizeExpanded = false },
                ) {
                    shoeSizeList.forEach { selectedSize ->
                        DropdownMenuItem(onClick = {
                            shoeSize = selectedSize
                            selectedShoeSize = shoeSizes.find { it.size.toString() == selectedSize }
                            sizeExpanded = false
                        }) {
                            Text(text = selectedSize)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Shoe Brand Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        DropdownMenuItem(onClick = {
                            shoeBrand = selectedBrand
                            selectedShoeBrand = shoeBrands.find { it.brandName == selectedBrand }
                            brandExpanded = false
                        }) {
                            Text(text = selectedBrand)
                        }
                    }
                }
            }

        }

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentStockLevel,
            onValueChange = { currentStockLevel = it },
            label = { Text("Current Stock Level") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = unitCost,
            onValueChange = { unitCost = it },
            label = { Text("Unit Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sellingPrice,
            onValueChange = { sellingPrice = it },
            label = { Text("Selling Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Show confirmation dialog before adding product
                showDialog = true
            }
        ) {
            Text("Add Product")
        }

    }
}
