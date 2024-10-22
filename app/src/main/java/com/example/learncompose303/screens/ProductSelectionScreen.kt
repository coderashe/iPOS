package com.example.learncompose303.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.learncompose303.R
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.Inventory
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
    var lowStockInventories by remember { mutableStateOf(emptyList<Inventory>()) } // List to hold low stock products
    var showNoSelectionWarning by remember { mutableStateOf(false) } // State for no product selected warning

    LaunchedEffect(key1 = true) {
        viewModel.fetchProducts() // Fetch products when the screen is launched

        // Check for low stock products
        val lowStock = products.filter { it.currentStockLevel < 10 }
        if (lowStock.isNotEmpty()) {
            lowStockInventories = lowStock // Save the low stock products
            showLowStockWarning = true // Trigger the popup
        }
    }

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
        LazyColumn {
            items(products.chunked(5)) { rowItems ->
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
                                inventory = product,
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
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
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


//@Composable
//fun ProductSelectionScreen(
//    customer: Customer,
//    onProceedToCart: () -> Unit,
//    navController: NavController,
//    viewModel: InvoiceViewModel = viewModel()
//) {
//    val products by viewModel.productList.collectAsState()
//    val selectedProducts by viewModel.selectedProducts.collectAsState() // Get selected products
//    var showLowStockWarning by remember { mutableStateOf(false) } // State for the popup
//    var lowStockProducts by remember { mutableStateOf(emptyList<Product>()) } // List to hold low stock products
//
//    LaunchedEffect(key1 = true) {
//        viewModel.fetchProducts() // Fetch products when the screen is launched
//
//        // Check for low stock products
//        val lowStock = products.filter { it.currentStockLevel < 10 }
//        if (lowStock.isNotEmpty()) {
//            lowStockProducts = lowStock // Save the low stock products
//            showLowStockWarning = true // Trigger the popup
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Row {
//            Text(
//                "Customer: ${customer.name}",
//                fontWeight = FontWeight.Bold,
//                fontSize = 18.sp,
//                modifier = Modifier.weight(1f)
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            Button(
//                onClick = onProceedToCart,
//            ) {
//                Text("Proceed to Cart")
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(products.chunked(5)) { rowItems ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .horizontalScroll(rememberScrollState()),
//                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                ) {
//                    for (product in rowItems) {
//                        val isSelected = selectedProducts.contains(product)
//                        val price = viewModel.getPricelistById(product.productId)
//                        if (price != null) {
//                            ProductCard(
//                                product = product,
//                                isSelected = isSelected,
//                                onAddProduct = {
//                                    viewModel.addProductToCart(product)
//                                },
//                                price = price.sellingPrice
//                            )
//                        } else {
//                            Log.d(
//                                "HazyLogs",
//                                "No price found for product ID: ${product.productId}"
//                            )
//                        }
//                    }
//                    if (rowItems.size == 1) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
//                }
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//    }
//
//    // Show a popup warning for low stock products
//    if (showLowStockWarning) {
//        AlertDialog(
//            onDismissRequest = { showLowStockWarning = false },
//            title = { Text("Low Stock Warning") },
//            text = {
//                Column {
//                    Text("The following products have low stock:")
//                    Spacer(modifier = Modifier.height(8.dp))
//                    lowStockProducts.forEach { product ->
//                        Text("- ${product.productDescription} (Stock: ${product.currentStockLevel})")
//                    }
//                }
//            },
//            confirmButton = {
//                Button(onClick = { showLowStockWarning = false }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}

@Composable
fun ProductCard(
    inventory: Inventory,
    isSelected: Boolean,
    onAddProduct: (Inventory) -> Unit,
    price: Double
) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageUri.value = uri
            launchHandlingActivity(context, uri)
        }
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp)
            .clickable {
                // Check if the product is in stock before adding
                if (inventory.currentStockLevel > 0) {
                    onAddProduct(inventory)
                } else {
                    Toast
                        .makeText(
                            context,
                            "${inventory.productDescription} is out of stock!...",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
        ) {
            // Product Image using Coil to load image from URI or String
            Image(
                painter = rememberImagePainter(data = inventory.imageUri ?: R.drawable.meat7),
                contentDescription = inventory.productDescription,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "\$${"%.2f".format(price)}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = inventory.productDescription,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp),
                    text = " ${"%.2f".format(inventory.currentStockLevel)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // Check stock before adding
                    if (inventory.currentStockLevel > 0) {
                        onAddProduct(inventory)
                    } else {
                        Toast.makeText(
                            context,
                            "${inventory.productDescription} is out of stock!...",
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
                    text = if (isSelected) "Added" else "Add to Cart",
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}
