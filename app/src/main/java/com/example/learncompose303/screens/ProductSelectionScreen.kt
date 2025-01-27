package com.example.learncompose303.screens

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
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

        // Check for low stock products
        val lowStock = products.filter { it.currentStockLevel < 10 }
        if (lowStock.isNotEmpty()) {
            lowStockInventories = lowStock // Save the low stock products
            showLowStockWarning = true // Trigger the popup
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
                                    price = price.sellingPrice
                                )
                            } else {
                                Log.d(
                                    "HazyLogs",
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
                        Text("- ${product.productDescription} (Stock: ${product.currentStockLevel})")
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
    price: Double
) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(4.dp)
            .clickable {
                // Check if the product is in stock before adding
                if (products.currentStockLevel > 0) {
                    onAddProduct(products)
                } else {
                    Toast
                        .makeText(
                            context,
                            "${products.productDescription} is out of stock!...",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = products.productDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Product Image using Coil to load image from URI or String
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Product Image
                val currentImageUri = imageUri.value ?: products.imageUri?.let { Uri.parse(it) }
                val painter = rememberImagePainter(data = currentImageUri ?: R.drawable.meat7)

                Image(
                    painter = painter,
                    contentDescription = products.productDescription,
                    modifier = Modifier
                        .size(90.dp)
                        .fillMaxWidth()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Stock Level Overlay
                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Align to the top end of the box
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    text = "%.1f".format(products.currentStockLevel), // Reduced decimal points
                    style = MaterialTheme.typography.bodySmall, // Smaller text style
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "\$${"%.2f".format(price)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    // Check stock before adding
                    if (products.currentStockLevel > 0) {
                        onAddProduct(products)
                    } else {
                        Toast.makeText(
                            context,
                            "${products.productDescription} is out of stock!...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    if (isSelected) Color.Red else Blue1
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isSelected) stringResource(R.string.added) else stringResource(R.string.addToCart),
                    color = if (isSelected) Color.White else Color.Black
                )
            }

        }
    }
}
