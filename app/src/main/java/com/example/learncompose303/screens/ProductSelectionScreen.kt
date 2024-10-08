package com.example.learncompose303.screens

import android.R
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.ui.theme.Blue1
import com.example.learncompose303.ui.theme.MyGray
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
                onClick = onProceedToCart,
            ) {
                Text("Proceed to Cart")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(products.chunked(3)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (product in rowItems) {
                        val isSelected = selectedProducts.contains(product)
                        val price = viewModel.getPricelistById(product.productId)
                        if (price != null) {
                            ProductCard(
                                product = product,
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
                        Spacer(modifier = Modifier.weight(1f)) // Fill the space for uneven row
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProductCard(
    product: Product,
    isSelected: Boolean,
    onAddProduct: (Product) -> Unit,
    price: Double
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(175.dp)
            .padding(8.dp)
            .clickable {
                // Check if the product is in stock before adding
                if (product.currentStockLevel > 0) {
                    onAddProduct(product)
                } else {
                    Toast
                        .makeText(
                            context,
                            "${product.productDescription} is out of stock!...",
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
            // Product Image
            product.imageResId?.let { painterResource(id = it) }?.let {
                Image(
                    painter = it,
                    contentDescription = product.productDescription,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("\$${price}", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = product.productDescription,
                    style = MaterialTheme.typography.bodySmall
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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    // Check stock before adding
                    if (product.currentStockLevel > 0) {
                        onAddProduct(product)
                    } else {
                        Toast.makeText(
                            context,
                            "${product.productDescription} is out of stock!...",
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


//
//@Composable
//fun ProductCard(
//    product: Product,
//    isSelected: Boolean,
//    onAddProduct: (Product) -> Unit,
//    price: Double
//) {
//
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .width(175.dp)
//            .padding(8.dp)
//            .clickable {
//                onAddProduct(product)
//            },
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(2.dp),
//        ) {
//            // Product Image
//            product.imageResId?.let { painterResource(id = it) }?.let {
//                Image(
//                    painter = it,
//                    contentDescription = product.productDescription,
//                    modifier = Modifier
//                        .size(100.dp)
//                        .clip(CircleShape)
//                        .align(Alignment.CenterHorizontally),
//                    contentScale = ContentScale.Crop
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("\$${price}", style = MaterialTheme.typography.headlineSmall)
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier
//            ) {
//                Text(
//                    modifier = Modifier.weight(1f),
//                    text = product.productDescription,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                Text(
//                    modifier = Modifier
//                        .background(
//                            color = Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(2.dp),
//                    text = product.currentStockLevel.toString(),
//                    style = MaterialTheme.typography.titleSmall,
//                    color = Color.Red,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//            Button(
//                modifier = Modifier.fillMaxWidth(),
//                onClick = {
//                    onAddProduct(product)
//                },
//                colors = ButtonDefaults.buttonColors(
//                    if (isSelected) Color.Red else Blue1
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = if (isSelected) "Added" else "Add to Cart",
//                    color = if (isSelected) Color.White else Color.Black
//                )
//            }
//        }
//    }
//}

