package com.example.learncompose303.screens

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
                items(filteredProducts.chunked(chunkSize)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (product in rowItems) {
                            val isSelected = selectedProducts.contains(product)
                            val price = viewModel.getPricelistById(product.productId)

                            if (price != null) {
                                ProductCard(
                                    products = product,
                                    isSelected = isSelected,
                                    onAddProduct = {
                                        viewModel.addProductToCart(product)
                                    },
                                    price = price.sellingPrice,
                                    viewModel = viewModel // Pass ViewModel directly
                                )
                            } else {
                                Log.d(
                                    "istart",
                                    "No price found for product ID: ${product.productId}"
                                )
                            }
                        }
                        // To align remaining products if rowItems is less than the chunk size
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No products found",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


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

@Composable
fun ProductCard(
    products: Products,
    isSelected: Boolean,
    onAddProduct: (Products) -> Unit,
    price: Double,
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    var stockLevel by remember { mutableStateOf(0L) }

    // Collect shoe size state from ViewModel
    val shoeSizeState = viewModel.getShoeSizeById(products.shoeSizeId)
        .collectAsState(initial = null)

    val shoeSize = shoeSizeState.value?.size?.toString() ?: "N/A"

    // Fetch stock level when productId changes
    LaunchedEffect(products.productId) {
        stockLevel = products.productQty
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable {
                if (stockLevel > 0) {
                    onAddProduct(products)
                } else {
                    Toast
                        .makeText(
                            context,
                            "${products.productDescription} is out of stock!",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            },
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Image Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Product Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = products.productDescription,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Size: $shoeSize",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Level
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Stock: $stockLevel",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (stockLevel > 0) MaterialTheme.colorScheme.primary else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$${"%.2f".format(price)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (stockLevel > 0) {
                        onAddProduct(products)
                    } else {
                        Toast.makeText(
                            context,
                            "${products.productDescription} is out of stock!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = if (isSelected) stringResource(R.string.added) else stringResource(R.string.addToCart),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
