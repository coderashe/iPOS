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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.learncompose303.data.entities.ShoeBrand
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
    val withBranProducts by viewModel.withBrandProductList.collectAsState()
    val selectedProducts by viewModel.selectedProducts.collectAsState()
    var showLowStockWarning by remember { mutableStateOf(false) }
    var lowStockInventories by remember { mutableStateOf(emptyList<Products>()) }
    var showNoSelectionWarning by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        viewModel.fetchProducts()
        viewModel. fetchBrandProducts()
        val lowStock = products.filter { it.productQty < 10 }
        if (lowStock.isNotEmpty()) {
            lowStockInventories = lowStock
            showLowStockWarning = true
        }
    }

    val query = searchQuery.trim().lowercase()
    val filteredProducts = withBranProducts.filter {
        it.product.productDescription.lowercase().contains(query) ||
                it.brand.brandName.lowercase().contains(query)
    }

//    old logic
//    val filteredProducts = products.filter {
//        it.productDescription.contains(searchQuery, ignoreCase = true)
//    }

    val configuration = LocalConfiguration.current
    val chunkSize = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 5 else 9

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
            Button(onClick = {
                if (selectedProducts.isNotEmpty()) {
                    onProceedToCart()
                } else {
                    showNoSelectionWarning = true
                }
            }) {
                Text("Proceed to Cart")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar(query = searchQuery, onQueryChanged = { searchQuery = it })
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredProducts.isNotEmpty()) {
            LazyColumn {
                items(filteredProducts.chunked(chunkSize)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { product ->
                            val isSelected = selectedProducts.contains(product.product)
                            val price = viewModel.getPricelistById(product.product.productId)

                            price?.let {
                                ProductCard(
                                    products = product.product,
                                    isSelected = isSelected,
                                    onAddProduct = { viewModel.addProductToCart(product.product) },
                                    price = it.sellingPrice,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text("No products found", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

    if (showLowStockWarning) {
        AlertDialog(
            onDismissRequest = { showLowStockWarning = false },
            title = { Text("Low Stock Warning") },
            text = {
                Column {
                    Text("The following products have low stock:")
                    Spacer(modifier = Modifier.height(8.dp))
                    lowStockInventories.forEach { product ->
                        Text(
                            "- ${product.productDescription} : ${product.productQty}",
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLowStockWarning = false }) { Text("OK") }
            }
        )
    }

    if (showNoSelectionWarning) {
        AlertDialog(
            onDismissRequest = { showNoSelectionWarning = false },
            title = { Text("No Products Selected") },
            text = { Text("Please select at least one product before proceeding to the cart.") },
            confirmButton = {
                Button(onClick = { showNoSelectionWarning = false }) { Text("OK") }
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

    var brand by remember { mutableStateOf<ShoeBrand?>(null) }

    // Use LaunchedEffect to collect the Flow in a coroutine
    LaunchedEffect(products.brandID) {
        viewModel.getBrandById(products.brandID).collect { fetchedBrand ->
            brand = fetchedBrand
        }
    }

    val shoeSize = shoeSizeState.value?.size?.toString() ?: "N/A"
    val brandName = brand?.brandName ?: "Unknown Brand"

    // Fetch stock level when productId changes
    LaunchedEffect(products.productId) {
        stockLevel = products.productQty
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(6.dp)
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
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Image Placeholder
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ShoppingCart,
//                    contentDescription = "Product Image",
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(48.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = products.productDescription,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "$brandName :- $shoeSize",
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    color = if (stockLevel > 0) MaterialTheme.colorScheme.primary else Color.Red,
                    modifier = Modifier.background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${"%.2f".format(price)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

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
