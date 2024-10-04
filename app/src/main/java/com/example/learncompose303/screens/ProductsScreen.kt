package com.example.learncompose303.screens

import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun ProductsScreen(
    viewModel: InvoiceViewModel = viewModel()
) {
    val products by viewModel.productList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        SearchBarExample()
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(products.chunked(3)) { rowItems ->
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
        Spacer(modifier = Modifier.height(16.dp))
    }


}
