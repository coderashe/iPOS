package com.example.learncompose303.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun ProductsScreen(
    viewModel: InvoiceViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()
    var searchQuery by remember { mutableStateOf("") } // State for the search query

    // Filter products based on the search query
    val filteredProducts = products.filter { product ->
        product.productDescription.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Use your custom SearchBar
        SearchBar(
            query = searchQuery,
            onQueryChanged = { searchQuery = it } // Update the search query
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Only display products if filteredProducts is not empty
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
                                ProductCard(
                                    product = product,
                                    isSelected = false,
                                    onAddProduct = {},
                                    price = price.sellingPrice
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f)) // Fill the space for uneven row
                        }
                    }
                }
            }
        } else {
            // Optionally, show a message if no products are found
            Text(
                text = "No products found",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
