package com.ipos.screens

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
import com.ipos.data.Destinations
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.viewmodels.InvoiceViewModel
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipos.R
import com.ipos.app.core.BusinessType
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
import com.ipos.viewmodels.SettingsViewModel

// ── Category options per business type ──────────────────────────────────────
private val GROCERY_CATEGORIES = listOf(
    "Food", "Drinks", "Sweets", "Household", "Personal Care", "Other"
)
private val GROCERY_UNITS = listOf(
    "each", "kg", "g", "litre", "ml", "pack", "dozen"
)
private val BUTCHER_CATEGORIES = listOf(
    "Beef", "Chicken", "Pork", "Lamb", "Fish", "Other"
)
private val BUTCHER_STOCK_UNITS = listOf("weight", "count")

@Composable
fun LoadStockScreen(
    viewModel: InvoiceViewModel,
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onProductAdded: () -> Unit
) {
    val businessType by settingsViewModel.businessType.collectAsState()

    // ── Shared fields ──────────────────────────────────────────────────────
    var productName by remember { mutableStateOf("") }
    var currentStockLevel by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── SHOES fields ───────────────────────────────────────────────────────
    val shoeSizes by viewModel.shoeSizes.collectAsState()
    val shoeBrands by viewModel.shoeBrands.collectAsState()
    var selectedShoeSize by remember { mutableStateOf<ShoeSize?>(null) }
    var selectedShoeBrand by remember { mutableStateOf<ShoeBrand?>(null) }
    var shoeSize by remember { mutableStateOf("3.0") }
    var shoeBrand by remember { mutableStateOf("OLDSTOCK") }
    var sizeExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }

    // ── GROCERY fields ─────────────────────────────────────────────────────
    var selectedGroceryCategory by remember { mutableStateOf(GROCERY_CATEGORIES.first()) }
    var groceryCategoryExpanded by remember { mutableStateOf(false) }
    var selectedGroceryUnit by remember { mutableStateOf(GROCERY_UNITS.first()) }
    var groceryUnitExpanded by remember { mutableStateOf(false) }

    // ── BUTCHER fields ─────────────────────────────────────────────────────
    var selectedButcherCategory by remember { mutableStateOf(BUTCHER_CATEGORIES.first()) }
    var butcherCategoryExpanded by remember { mutableStateOf(false) }
    var selectedStockUnit by remember { mutableStateOf(BUTCHER_STOCK_UNITS.first()) }
    var stockUnitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchShoeBrands()
        viewModel.fetchShoeSizes()
    }

    val requestFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let { stream ->
                viewModel.uploadProductsFromCsv(stream, context)
            }
        }
    }

    // ── Confirm dialog ─────────────────────────────────────────────────────
    if (showDialog) {
        // Shoes duplicate-check
        if (businessType == BusinessType.SHOES) {
            LaunchedEffect(productName, selectedShoeSize) {
                val product = viewModel.getProductByNameAndSize(
                    productName, selectedShoeSize?.shoeSizeId ?: 0
                )
                if (product != null) {
                    errorMessage = "This shoe already exists in the inventory with the same name and size."
                    showDialog = false
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } else {
                    errorMessage = null
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirm Adding Product") },
            text = {
                Column {
                    Text("Are you sure you want to add this product to the inventory?")
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = Color.Red)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val unitCostValue = unitCost.toDoubleOrNull()
                        val sellingPriceValue = sellingPrice.toDoubleOrNull()
                        val currentStockLevelValue = currentStockLevel.toLongOrNull()

                        val validationError: String? = when {
                            productName.isBlank() -> "Product name is required."
                            unitCostValue == null -> "Enter a valid unit cost."
                            sellingPriceValue == null -> "Enter a valid selling price."
                            currentStockLevelValue == null -> "Enter a valid stock level."
                            businessType == BusinessType.SHOES && selectedShoeSize == null -> "Select a shoe size."
                            businessType == BusinessType.SHOES && selectedShoeBrand == null && shoeBrands.isEmpty() -> "No brands available."
                            else -> null
                        }

                        if (validationError != null) {
                            Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                            showDialog = false
                            return@TextButton
                        }

                        // Resolve brand for shoes
                        if (businessType == BusinessType.SHOES && selectedShoeBrand == null && shoeBrands.isNotEmpty()) {
                            selectedShoeBrand = shoeBrands.first()
                        }

                        val totalCost = unitCostValue!! * currentStockLevelValue!!

                        val newProduct = when (businessType) {
                            BusinessType.SHOES -> Products(
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
                            BusinessType.GROCERY -> Products(
                                productDescription = productName,
                                saleable = true,
                                purchasable = true,
                                averageCost = totalCost,
                                standardCost = unitCostValue,
                                salesTaxCode = "VAT",
                                productCode = "ISTART32",
                                inclusiveTaxed = true,
                                productQty = currentStockLevelValue,
                                category = selectedGroceryCategory,
                                unit = selectedGroceryUnit
                            )
                            BusinessType.BUTCHER -> Products(
                                productDescription = productName,
                                saleable = true,
                                purchasable = true,
                                averageCost = totalCost,
                                standardCost = unitCostValue,
                                salesTaxCode = "VAT",
                                productCode = "ISTART32",
                                inclusiveTaxed = true,
                                productQty = currentStockLevelValue,
                                category = selectedButcherCategory,
                                stockUnit = selectedStockUnit
                            )
                            null -> Products(
                                productDescription = productName,
                                saleable = true,
                                purchasable = true,
                                averageCost = totalCost,
                                standardCost = unitCostValue,
                                salesTaxCode = "VAT",
                                productCode = "ISTART32",
                                inclusiveTaxed = true,
                                productQty = currentStockLevelValue
                            )
                        }

                        viewModel.insertTheProduct(newProduct) { productId ->
                            val newPriceList = PriceList(
                                productId = productId,
                                productDescription = productName,
                                unitCost = unitCostValue,
                                sellingPrice = sellingPriceValue!!,
                                markupPercentage = 0.0,
                                grossProfitPercentage = 0.0
                            )
                            viewModel.insertPriceList(newPriceList)
                            onProductAdded()
                            Toast.makeText(context, "Product and Price added!", Toast.LENGTH_SHORT).show()
                            productName = ""
                            unitCost = ""
                            currentStockLevel = ""
                            sellingPrice = ""
                            selectedShoeSize = null
                            selectedShoeBrand = null
                            navController.navigate(Destinations.Dashboard.route)
                        }

                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.primary)
                ) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
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

    // ── Main form ──────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top action cards (Shoe Mgt | CSV Upload)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (businessType == BusinessType.SHOES) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { navController.navigate(Destinations.ShoeManagement.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                    ),
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
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { requestFileLauncher.launch("*/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                ),
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── Business-type-specific fields ──────────────────────────────────
        when (businessType) {

            // ── SHOES ───────────────────────────────────────────────────────
            BusinessType.SHOES -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Shoe Size dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = shoeSize,
                            onValueChange = {},
                            label = { Text("Shoe Size") },
                            readOnly = true,
                            modifier = Modifier.clickable { sizeExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { sizeExpanded = !sizeExpanded }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = sizeExpanded,
                            onDismissRequest = { sizeExpanded = false }
                        ) {
                            shoeSizes.forEach { size ->
                                DropdownMenuItem(onClick = {
                                    shoeSize = size.size.toString()
                                    selectedShoeSize = size
                                    sizeExpanded = false
                                }) { Text(text = size.size.toString()) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Shoe Brand dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = shoeBrand,
                            onValueChange = {},
                            label = { Text("Brand / Product Code") },
                            readOnly = true,
                            modifier = Modifier.clickable { brandExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { brandExpanded = !brandExpanded }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            shoeBrands.forEach { brand ->
                                DropdownMenuItem(onClick = {
                                    shoeBrand = brand.brandName
                                    selectedShoeBrand = brand
                                    brandExpanded = false
                                }) { Text(text = brand.brandName) }
                            }
                        }
                    }
                }
            }

            // ── GROCERY ─────────────────────────────────────────────────────
            BusinessType.GROCERY -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Category dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedGroceryCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            modifier = Modifier.clickable { groceryCategoryExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        groceryCategoryExpanded = !groceryCategoryExpanded
                                    }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = groceryCategoryExpanded,
                            onDismissRequest = { groceryCategoryExpanded = false }
                        ) {
                            GROCERY_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(onClick = {
                                    selectedGroceryCategory = cat
                                    groceryCategoryExpanded = false
                                }) { Text(cat) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Unit dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedGroceryUnit,
                            onValueChange = {},
                            label = { Text("Unit") },
                            readOnly = true,
                            modifier = Modifier.clickable { groceryUnitExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        groceryUnitExpanded = !groceryUnitExpanded
                                    }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = groceryUnitExpanded,
                            onDismissRequest = { groceryUnitExpanded = false }
                        ) {
                            GROCERY_UNITS.forEach { u ->
                                DropdownMenuItem(onClick = {
                                    selectedGroceryUnit = u
                                    groceryUnitExpanded = false
                                }) { Text(u) }
                            }
                        }
                    }
                }
            }

            // ── BUTCHER ─────────────────────────────────────────────────────
            BusinessType.BUTCHER -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Category dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedButcherCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            modifier = Modifier.clickable { butcherCategoryExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        butcherCategoryExpanded = !butcherCategoryExpanded
                                    }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = butcherCategoryExpanded,
                            onDismissRequest = { butcherCategoryExpanded = false }
                        ) {
                            BUTCHER_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(onClick = {
                                    selectedButcherCategory = cat
                                    butcherCategoryExpanded = false
                                }) { Text(cat) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Stock unit dropdown (weight / count)
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedStockUnit,
                            onValueChange = {},
                            label = { Text("Stock Unit") },
                            readOnly = true,
                            modifier = Modifier.clickable { stockUnitExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        stockUnitExpanded = !stockUnitExpanded
                                    }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = stockUnitExpanded,
                            onDismissRequest = { stockUnitExpanded = false }
                        ) {
                            BUTCHER_STOCK_UNITS.forEach { su ->
                                DropdownMenuItem(onClick = {
                                    selectedStockUnit = su
                                    stockUnitExpanded = false
                                }) { Text(su) }
                            }
                        }
                    }
                }
            }

            null -> {
                // Business type still loading from SharedPreferences — show nothing, renders in ms
            }
        }

        // ── Shared text fields ─────────────────────────────────────────────
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Description") },
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
        Button(onClick = { showDialog = true }) {
            Text("Add Product")
        }
    }
}
