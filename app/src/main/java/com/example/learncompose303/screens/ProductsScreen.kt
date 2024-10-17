package com.example.learncompose303.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.ui.theme.Blue1
import com.example.learncompose303.viewmodels.InvoiceViewModel


@Composable
fun ProductsScreen(
    viewModel: InvoiceViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // State for the search query
    var showLowStockWarning by remember { mutableStateOf(false) } // State for the popup
    var lowStockProducts by remember { mutableStateOf(emptyList<Product>()) } // List to hold low stock products

    LaunchedEffect(key1 = true) {
        viewModel.fetchProducts() // Fetch products when the screen is launched

        // Check for low stock products
        val lowStock = products.filter { it.currentStockLevel < 10 }
        if (lowStock.isNotEmpty()) {
            lowStockProducts = lowStock // Save the low stock products
            showLowStockWarning = true // Trigger the popup
        }
    }

    // Filter products based on the search query
    val filteredProducts = products.filter { product ->
        product.productDescription.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChanged = { searchQuery = it } // Update the search query
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredProducts.isNotEmpty()) {
            LazyColumn {
                items(filteredProducts.chunked(3)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        for (product in rowItems) {
                            val price = viewModel.getPricelistById(product.productId)
                            if (price != null) {
                                TheProductCard(
                                    product = product,
                                    initialPrice = price.sellingPrice,
                                    viewModel
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f)) // Fill the space for uneven rows
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
                    lowStockProducts.forEach { product ->
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
}

@Composable
fun TheProductCard(
    product: Product,
    initialPrice: Double, // Use 'initialPrice' to avoid confusion with state variable
    viewModel: InvoiceViewModel = viewModel()
) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf(product.imageUri?.let { Uri.parse(it) }) }
    val isEditing = remember { mutableStateOf(false) } // Track edit state
    var editedProductDescription by remember { mutableStateOf(product.productDescription) }
    var editedPrice by remember { mutableStateOf(initialPrice.toString()) }

    // Make price a state variable to trigger recompositions
    var price by remember { mutableStateOf(initialPrice) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageUri.value = uri
            if (uri != null) {
                viewModel.onUpdateProductImage(product, uri, context)
            }
            viewModel.updateProduct(product)
        }
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Image
            val currentImageUri = imageUri.value ?: product.imageUri?.let { Uri.parse(it) }
            val painter = rememberImagePainter(data = currentImageUri ?: R.drawable.food2)

            Image(
                painter = painter,
                contentDescription = product.productDescription,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing.value) {
                // Editable Price Field
                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = { editedPrice = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                // Display Price - real-time updates using reactive price variable
                Text("\$${price}", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Description and Stock Level
            if (isEditing.value) {
                // Editable Product Description
                OutlinedTextField(
                    value = editedProductDescription,
                    onValueChange = { editedProductDescription = it },
                    label = { Text("Product Description") }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.productDescription,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp),
                        text = product.currentStockLevel.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Edit/Save Button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (isEditing.value) {
                        // Update product regardless of whether the description changed
                        val updatedProduct = product.copy(
                            productDescription = editedProductDescription
                        )

                        // Insert/update the product and handle the product ID to update the price list
                        viewModel.insertTheProduct(updatedProduct) { productId ->
                            // Update the price list even if the product description is unchanged
                            val updatedPriceList = PriceList(
                                productId = productId, // Use the productId for updating PriceList
                                productDescription = updatedProduct.productDescription, // Ensure price list gets updated with description
                                unitCost = product.standardCost,
                                sellingPrice = editedPrice.toDouble(), // Always update the price
                                markupPercentage = viewModel.calculateMarkupPercentage(
                                    product.standardCost,
                                    editedPrice.toDouble()
                                ),
                                grossProfitPercentage = viewModel.calculateGrossProfitPercentage(
                                    product.standardCost,
                                    editedPrice.toDouble()
                                )
                            )

                            // Insert/update the PriceList after updating the product
                            viewModel.insertCurrentPriceList(
                                updatedPriceList.sellingPrice, // Always update price
                                updatedPriceList.productDescription, // Always ensure description is updated
                                productId, // Use productId from updatedProduct
                                context
                            )

                            // Update the price state to trigger UI recomposition
                            price = updatedPriceList.sellingPrice

                            Toast.makeText(
                                context,
                                "Product and PriceList updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    isEditing.value = !isEditing.value // Toggle between edit and view mode
                },
                colors = ButtonDefaults.buttonColors(
                    if (isEditing.value) Color.Green else Blue1
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEditing.value) "Save" else "Edit",
                    color = if (isEditing.value) Color.White else Color.Black
                )
            }
        }
    }
}
