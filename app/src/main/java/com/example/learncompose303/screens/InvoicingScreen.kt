package com.example.learncompose303.screens

import android.R
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.data.Product
import com.example.learncompose303.ui.theme.MyGray


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InvoicingScreen() {
    var cartItemCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoicing") },
                actions = {
                    IconButton(onClick = { /* Open cart screen */ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_add), // Replace with cart icon
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$cartItemCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },

        content = { paddingValues -> // Use paddingValues provided by Scaffold
            Box(modifier = Modifier.padding(paddingValues)) { // Apply padding to avoid overlap
                ProductList(
                    products = listOf(
                        Product("Product 1", "$10.99", R.drawable.ic_menu_gallery),
                        Product("Product 2", "$14.99", android.R.drawable.ic_menu_gallery),
                        Product("Product 3", "$7.99", android.R.drawable.ic_menu_compass)
                    ),
                    onAddToCart = { cartItemCount++ }
                )
            }
        }
    )
}

@Composable
fun ProductList(products: List<Product>, onAddToCart: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products.chunked(3)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (product in rowItems) {
                    ProductItem(product = product, onAddToCart = onAddToCart)
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f)) // Fill the space for uneven row
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(165.dp)
            .height(220.dp)
            .background(MyGray),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
        ) {
            // Product Image
            Image(
                painter = painterResource(id = product.imageResId),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.price,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                // Add to Cart Button
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, shape = CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add to Cart",
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewInvoicingScreen() {
    InvoicingScreen()
}