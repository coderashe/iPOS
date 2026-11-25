package com.ipos.screens

import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.ipos.R
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.data.entities.ShoeBrand
import com.ipos.screens.auth.AuthViewModel
import com.ipos.ui.theme.MyBlueish
import com.ipos.viewmodels.InvoiceViewModel


@Composable
fun ProductsScreen(
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()
    val withBranProducts by viewModel.withBrandProductList.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // State for the search query
    var showLowStockWarning by remember { mutableStateOf(false) } // State for the popup

    // Compute low-stock inventories when `products` updates
    val lowStockInventories = remember(products) {
        viewModel.fetchBrandProducts()
        products.filter { product ->
            product.productQty < 10 // Using productQty directly
        }
    }

    // Show low-stock warning only when there are low-stock products
    LaunchedEffect(lowStockInventories) {
        showLowStockWarning = lowStockInventories.isNotEmpty()
        if (showLowStockWarning) {
        }
    }

    // Filter products based on the search query
    val query = searchQuery.trim().lowercase()
    val filteredProducts = withBranProducts.filter {
        it.product.productDescription.lowercase().contains(query) ||
                it.brand.brandName.lowercase().contains(query)
    }

//    old logic
//    val filteredProducts = products.filter { product ->
//        product.productDescription.contains(searchQuery, ignoreCase = true)
//    }

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
                            val price = viewModel.getPricelistById(product.product.productId)
                            if (price != null) {
                                TheProductCard(
                                    products = product.product,
                                    initialPrice = price.sellingPrice,
                                    viewModel = viewModel,
                                    authViewModel = authViewModel
                                )
                            } else {

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
                        Text(
                            text = "- ${product.productDescription} : ${product.productQty}",
                            color = Color.Red
                        )
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
    viewModel: InvoiceViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val userRole by authViewModel.userRole.observeAsState()
    val isAdmin = userRole == "admin"
    val context = LocalContext.current
    var stockLevel by remember { mutableStateOf(0L) }
    val imageUri = remember { mutableStateOf(products.imageUri?.let { Uri.parse(it) }) }
    val isEditing = remember { mutableStateOf(false) }
    var editedProductDescription by remember { mutableStateOf(products.productDescription) }
    var editedPrice by remember { mutableStateOf(initialPrice.toString()) }
    var price by remember { mutableStateOf(initialPrice) }

    // Collect shoe size state from ViewModel (shoeSizeId is nullable for non-shoe products)
    val shoeSizeState = products.shoeSizeId?.let {
        viewModel.getShoeSizeById(it).collectAsState(initial = null)
    }

//    fetch brandName
    var brand by remember { mutableStateOf<ShoeBrand?>(null) }
    // Use LaunchedEffect to collect the Flow in a coroutine
    LaunchedEffect(products.brandID) {
        products.brandID?.let { brandId ->
            viewModel.getBrandById(brandId).collect { fetchedBrand ->
                brand = fetchedBrand
            }
        }
    }
    val brandName = brand?.brandName ?: "Unknown Brand"
    val shoeSize = shoeSizeState?.value?.size?.toString() ?: "N/A"

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
            .shadow(8.dp, RoundedCornerShape(16.dp)),
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
//            Text(
//                text = "Size: $shoeSize",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
            Text(
                text = "$brandName :- $shoeSize",
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.background(Color.White)
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    if (isEditing.value) Color.Green else MyBlueish
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


