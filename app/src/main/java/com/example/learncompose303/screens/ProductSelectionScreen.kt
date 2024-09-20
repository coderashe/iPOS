package com.example.learncompose303.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun ProductSelectionScreen(
    customer: Customer,
    onAddProduct: (Product) -> Unit,
    onProceedToCart: () -> Unit,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
//    fetch all products
    val products by viewModel.productList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Selected Customer: ${customer.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(products) { product ->
                ProductCard(product = product, onAddProduct = onAddProduct)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onProceedToCart,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Proceed to Cart")
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddProduct: (Product) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onAddProduct(product) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.name, fontWeight = FontWeight.Bold)
            Text("Price: \$${product.price}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onAddProduct(product) }) {
                Text("Add to Cart")
            }
        }
    }
}

@Preview
@Composable
fun PrevProductScreen() {

}
