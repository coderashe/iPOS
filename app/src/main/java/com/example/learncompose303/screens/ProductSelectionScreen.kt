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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
                Text(
                    "Proceed to Cart",
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(products) { product ->
                ProductCard(product = product, onAddProduct = {
                    // Add the selected product to cart
                    viewModel.addProductToCart(product)
                })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun ProductCard(product: Product, onAddProduct: (Product) -> Unit) {
    var isChecked by remember { mutableStateOf(false) }
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
            Button(
                onClick = {
                    onAddProduct(product)
                    isChecked = true
                },
                colors = ButtonDefaults.buttonColors(
                    if (isChecked) Color.Red else Blue1
                )
            ) {
                Text(
                    text = if (isChecked) "Added" else "Add to Cart",
                    color = if (isChecked) Color.White else Color.Black
                )
            }
        }
    }
}

@Preview
@Composable
fun PrevProductScreen() {

    val viewModel: InvoiceViewModel = viewModel()
    val navController = rememberNavController()

    // Sample data
    val customer =
        Customer(
            customerId = 1,
            name = "John Doe",
            email = "john.doe@example.com",
            "0769853534",
            "State House"
        )

    ProductSelectionScreen(
        customer = customer,
        {},
        {},
        navController,
        viewModel
    )
}
