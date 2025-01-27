package com.example.learncompose303.screens

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.ui.theme.Blue1
import com.example.learncompose303.viewmodels.InvoiceViewModel


@Composable
fun ProductsScreen(
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // State for the search query
    var showLowStockWarning by remember { mutableStateOf(false) } // State for the popup
    var lowStockInventories by remember { mutableStateOf(emptyList<Products>()) } // List to hold low stock products

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
            .padding(8.dp)
    ) {
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
                            val price = viewModel.getPricelistById(product.productId)
                            if (price != null) {
                                TheProductCard(
                                    products = product,
                                    initialPrice = price.sellingPrice,
                                    viewModel,
                                    authViewModel
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
}

@Composable
fun TheProductCard(
    products: Products,
    initialPrice: Double,
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf(products.imageUri?.let { Uri.parse(it) }) }
    val isEditing = remember { mutableStateOf(false) }
    var editedProductDescription by remember { mutableStateOf(products.productDescription) }
    var editedPrice by remember { mutableStateOf(initialPrice.toString()) }
    var price by remember { mutableStateOf(initialPrice) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageUri.value = uri
            if (uri != null) {
                viewModel.onUpdateProductImage(products, uri, context)
            }
            viewModel.updateProduct(products)
        }
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Product Description
            if (isEditing.value) {
                OutlinedTextField(
                    value = editedProductDescription,
                    onValueChange = { editedProductDescription = it },
                    label = { Text("Product Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = products.productDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing

            // Box to overlay the stock level on the product image
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
                        .clip(CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentScale = ContentScale.Crop
                )

                // Stock Level Overlay
                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Align to the top end of the box
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    text = "${"%.1f".format(products.currentStockLevel)}", // Reduced decimal points
                    style = MaterialTheme.typography.bodySmall, // Smaller text style
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            // Price Display or Edit Field
            if (isEditing.value) {
                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = { editedPrice = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth() // Full width for better usability
                )
            } else {
                Text(
                    "\$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing

            val userRole by authViewModel.userRole.observeAsState()
            val isAdmin = userRole == "admin"

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (isAdmin) {  // Only allow if the user is an admin
                        if (isEditing.value) {
                            val updatedProduct = products.copy(
                                productDescription = editedProductDescription
                            )
                            viewModel.insertTheProduct(updatedProduct) { productId ->
                                val updatedPriceList = PriceList(
                                    productId = productId,
                                    productDescription = updatedProduct.productDescription,
                                    unitCost = products.standardCost,
                                    sellingPrice = editedPrice.toDouble(),
                                    markupPercentage = viewModel.calculateMarkupPercentage(
                                        products.standardCost,
                                        editedPrice.toDouble()
                                    ),
                                    grossProfitPercentage = viewModel.calculateGrossProfitPercentage(
                                        products.standardCost,
                                        editedPrice.toDouble()
                                    )
                                )
                                viewModel.insertCurrentPriceList(
                                    updatedPriceList.sellingPrice,
                                    updatedPriceList.productDescription,
                                    productId,
                                    context
                                )
                                price = updatedPriceList.sellingPrice

                                Toast.makeText(
                                    context,
                                    "Product and PriceList updated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        isEditing.value = !isEditing.value
                    } else {
                        // Show a message or prevent action for non-admins
                        Toast.makeText(
                            context,
                            "Only admins can edit stock.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = isAdmin,  // Disable the button for non-admin users
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
