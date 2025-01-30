package com.example.learncompose303.screens

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.ui.theme.Blue1
import com.example.learncompose303.viewmodels.InvoiceViewModel


@Composable
fun ProductSelectionScreen(
    customer: Customer,
    onProceedToCart: () -> Unit,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()
    val selectedProducts by viewModel.selectedProducts.collectAsState() // Get selected products
    var showLowStockWarning by remember { mutableStateOf(false) } // State for the popup
    var lowStockInventories by remember { mutableStateOf(emptyList<Products>()) } // List to hold low stock products
    var showNoSelectionWarning by remember { mutableStateOf(false) } // State for no product selected warning

    var searchQuery by remember { mutableStateOf("") } // State for the search query

    LaunchedEffect(key1 = true) {
        viewModel.fetchProducts() // Fetch products when the screen is launched

        val updatedProducts = products.map { product ->
            val currentStock =
                viewModel.getCurrentStockByBrand(product.brandID) // Fetch current stock
            product.copy(
                productDescription = product.productDescription, // Keep other fields intact
                brandID = product.brandID,
                shoeSizeId = product.shoeSizeId,
                productQty = product.productQty
                // Optionally, add the current stock to a custom field in the Product entity or manage it separately
            )
        }

        // Check for low stock products
        val lowStock = updatedProducts.filter { product ->
            // Replace the condition with your low stock logic (e.g., based on size or quantity)
            val currentStock = viewModel.getCurrentStockByBrand(product.brandID)
            currentStock < 10
        }

        if (lowStock.isNotEmpty()) {
            lowStockInventories = lowStock
            Log.d("istart", "Check for lowStocks --- $lowStock")
            showLowStockWarning = true
        }
    }

    // Filter products based on the search query
    val filteredProducts = products.filter { product ->
        product.productDescription.contains(searchQuery, ignoreCase = true)
    }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val chunkSize = if (isPortrait) 5 else 9  // Set chunk size based on orientation


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            Text(
                "Customer: ${customer.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (selectedProducts.isNotEmpty()) {
                        onProceedToCart() // Proceed if products are selected
                    } else {
                        showNoSelectionWarning = true // Show warning if no products selected
                    }
                }
            ) {
                Text("Proceed to Cart")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar(
            query = searchQuery,
            onQueryChanged = { searchQuery = it } // Update the search query
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Group products by brand
        val groupedProducts = filteredProducts.groupBy { it.brandID }

        if (filteredProducts.isNotEmpty()) {
            LazyColumn {
                items(groupedProducts.entries.toList()) { (brandId, brandProducts) ->
                    var fetchedBrandName by remember { mutableStateOf("Loading...") } // Default value

                    // Fetch brand name asynchronously
                    LaunchedEffect(brandId) {
                        fetchedBrandName = viewModel.getBrandNameById(brandId)
                    }

                    ProductCard(
                        brandName = fetchedBrandName, // Use fetched brand name
                        products = brandProducts,
                        selectedProduct = brandProducts.firstOrNull(),
                        onProductSelected = { viewModel.addProductToCart(it) },
                        price = brandProducts.firstOrNull()?.standardCost ?: 0.0,
                        fetchStockLevel = { viewModel.fetchStockLevel(brandId) }
                    )
                }
            }
        } else {
            Text(
                text = "No products found",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

//        if (filteredProducts.isNotEmpty()) {
//            LazyColumn {
//                items(filteredProducts.chunked(chunkSize)) { rowItems ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .horizontalScroll(rememberScrollState()),
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        for (product in rowItems) {
//                            val isSelected = selectedProducts.contains(product)
//                            val price = viewModel.getPricelistById(product.productId)
//                            val groupedProducts = products.groupBy { it.brandID }
//
//                            if (price != null) {
//                                ProductCard(
//                                    products = product,
//                                    isSelected = isSelected,
//                                    onAddProduct = {
//                                        viewModel.addProductToCart(product)
//                                    },
//                                    price = price.sellingPrice,
//                                    fetchStockLevel = { brandId -> viewModel.fetchStockLevel(brandId) }
//                                )
//                            } else {
//                                Log.d(
//                                    "istart",
//                                    "No price found for product ID: ${product.productId}"
//                                )
//                            }
//
//                            //
//                        }
//
//                        // To align remaining products if rowItems is less than the chunk size
//                        if (rowItems.size == 1) {
//                            Spacer(modifier = Modifier.weight(1f))
//                        }
//                    }
//                }
//            }
//        } else {
//            Text(
//                text = "No products found",
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
//        }


        Spacer(modifier = Modifier.height(16.dp))
    }

    // Show a popup warning for low stock products
    if (showLowStockWarning) {
        AlertDialog(
            onDismissRequest = { showLowStockWarning = false },
            title = { Text("Low Stock Warning") },
            text = {
                Column {
                    Text("The following products have low stock:")
                    Spacer(modifier = Modifier.height(8.dp))
                    lowStockInventories.forEach { product ->
                        Text("- ${product.productDescription} (Stock: ---)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLowStockWarning = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Show a warning if no products are selected
    if (showNoSelectionWarning) {
        AlertDialog(
            onDismissRequest = { showNoSelectionWarning = false },
            title = { Text("No Products Selected") },
            text = { Text("Please select at least one product before proceeding to the cart.") },
            confirmButton = {
                Button(onClick = { showNoSelectionWarning = false }) {
                    Text("OK")
                }
            }
        )
    }
}

//@Composable
//fun ProductCard(
//    brandName: String,
//    products: List<Products>,
//    selectedProduct: Products?,
//    onProductSelected: (Products) -> Unit,
//    price: Double,
//    fetchStockLevel: suspend (Long) -> Int
//) {
//    val context = LocalContext.current
//    var expanded by remember { mutableStateOf(false) }
//    var stockLevel by remember { mutableStateOf(0) }
//
//    LaunchedEffect(selectedProduct?.brandID) {
//        selectedProduct?.let {
//            stockLevel = fetchStockLevel(it.brandID)
//        }
//    }
//
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(12.dp)
//            .clip(RoundedCornerShape(16.dp))
//            .background(Color.White)
//            .shadow(6.dp, RoundedCornerShape(16.dp)),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // 🏷 Brand Name
//            Text(
//                text = brandName.uppercase(),
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 🔽 Product Dropdown
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.surface)
//            ) {
//                TextButton(
//                    onClick = { expanded = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = selectedProduct?.let { "${it.productDescription} | Size: ${it.shoeSizeId}" }
//                            ?: "Select a product",
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    products.forEach { product ->
//                        DropdownMenuItem(
//                            text = { Text("${product.productDescription} | Size: ${product.shoeSizeId}") },
//                            onClick = {
//                                expanded = false
//                                onProductSelected(product)
//                            }
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 📦 Stock Level Indicator
//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(if (stockLevel > 0) Color(0xFF4CAF50) else Color(0xFFD32F2F))
//                    .padding(horizontal = 12.dp, vertical = 4.dp)
//            ) {
//                Text(
//                    text = if (stockLevel > 0) "In Stock ($stockLevel)" else "Out of Stock",
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 💰 Price Display
//            Text(
//                text = "$${"%.2f".format(price)}",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.secondary
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 🛒 Add to Cart Button
//            ElevatedButton(
//                onClick = {
//                    if (selectedProduct != null && stockLevel > 0) {
//                        onProductSelected(selectedProduct)
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "Please select a product or check stock availability!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(if (stockLevel > 0) Color(0xFF008CFF) else Color.Gray),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = if (selectedProduct != null) "Add to Cart" else "Select Product",
//                    color = Color.White
//                )
//            }
//        }
//    }
//}

@Composable
fun ProductCard(
    brandName: String,
    products: List<Products>,
    selectedProduct: Products?,
    onProductSelected: (Products) -> Unit,
    price: Double,
    fetchStockLevel: suspend (Long) -> Int
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var stockLevel by remember { mutableStateOf(0) }

    LaunchedEffect(selectedProduct?.brandID) {
        selectedProduct?.let {
            stockLevel = fetchStockLevel(it.brandID)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            //
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.End)
                    .background(if (stockLevel > 0) Color(0xFF4CAF50) else Color(0xFFD32F2F))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (stockLevel > 0) "In Stock ($stockLevel)" else "Out of Stock",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // 🔽 Product Dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedProduct?.let { "${it.productDescription} | Size: ${it.shoeSizeId}" }
                            ?: "Select a product",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text("${product.productDescription} | Size: ${product.shoeSizeId}") },
                            onClick = {
                                expanded = false
                                onProductSelected(product)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Column for product details
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // 🖼️ Image at the start
                    Image(
                        painter = painterResource(id = R.drawable.bootsu), // Replace with your image
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                Column {
                    Row {
                        // 🏷 Brand Name and Stock Level
                        Text(
                            text = brandName.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                        )

                        // 💰 Price Display
                        Text(
                            text = "$${"%.2f".format(price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                    //            // 🛒 Add to Cart Button
                    ElevatedButton(
                        onClick = {
                            if (selectedProduct != null && stockLevel > 0) {
                                onProductSelected(selectedProduct)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select a product or check stock availability!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(if (stockLevel > 0) Color(0xFF008CFF) else Color.Gray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedProduct != null) "Add to Cart" else "Select Product",
                            color = Color.White
                        )
                    }
                }
            }

        }

    }
}


// old working
//@Composable
//fun ProductCard(
//    products: Products,
//    isSelected: Boolean,
//    onAddProduct: (Products) -> Unit,
//    price: Double,
//    fetchStockLevel: suspend (Long) -> Int // Accepts a function to fetch stock by brandId
//) {
//    val context = LocalContext.current
//    var stockLevel by remember { mutableStateOf(0) }
//
//    // Fetch stock level when the brandId changes
//    LaunchedEffect(products.productId) {
//        stockLevel = fetchStockLevel(products.productId)
//    }
//
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .padding(4.dp)
//            .clickable {
//                if (stockLevel > 0) {
//                    onAddProduct(products)
//                } else {
//                    Toast
//                        .makeText(
//                            context,
//                            "${products.productDescription} is out of stock!",
//                            Toast.LENGTH_SHORT
//                        )
//                        .show()
//                }
//            },
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(2.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                modifier = Modifier.fillMaxWidth(),
//                text = products.productDescription,
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Box(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                // Use stockLevel dynamically fetched
//                Text(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .background(Color.White, shape = RoundedCornerShape(8.dp))
//                        .padding(2.dp),
//                    text = stockLevel.toString(),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                "\$${"%.2f".format(price)}",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Button(
//                onClick = {
//                    if (stockLevel > 0) {
//                        onAddProduct(products)
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "${products.productDescription} is out of stock!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    if (isSelected) Color.Red else Blue1
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = if (isSelected) stringResource(R.string.added) else stringResource(R.string.addToCart),
//                    color = if (isSelected) Color.White else Color.Black
//                )
//            }
//        }
//    }
//}
