package com.example.learncompose303.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.More
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.viewmodels.InvoiceViewModel

@Composable
fun LoadStockScreen(
    viewModel: InvoiceViewModel,
    navController: NavController,
    onProductAdded: () -> Unit // Callback when a product is added
) {
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var imageResId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val productRequestFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let {
                viewModel.uploadProductsFromCsv(it, context)
            }
        }
    }

    val pricelistRequestFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let {
                viewModel.uploadPriceListsFromCsv(it, context)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Product Name Input
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Product Price Input
        OutlinedTextField(
            value = productPrice,
            onValueChange = { productPrice = it },
            label = { Text("Product Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                productRequestFileLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Products")
        }

        Spacer(modifier = Modifier.height(16.dp))

//        for priceListCsvUpload

        QuickAccessCard(
            label = stringResource(R.string.uploadPricelists),
            subTitle = "--",
            icon = Icons.Default.More,
            onClick = {
                pricelistRequestFileLauncher.launch("*/*")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to Add Product
        Button(
            onClick = {
                val price = productPrice.toDoubleOrNull()
                if (productName.isNotEmpty() && price != null) {
                    val newProduct = Product(
                        productDescription = productName,
                        currentStockLevel = 100.00,
                        saleable = true,
                        purchasable = true,
                        averageCost = 8.0,
                        standardCost = 10.0,
                        salesTaxCode = "VAT",
                        purchasableTaxCode = "VAT",
                        inclusiveTaxed = true,
                    )

                    viewModel.insertProduct(newProduct) // Add product to database
                    onProductAdded()
                    Toast.makeText(context, "Product added!", Toast.LENGTH_SHORT).show()

                    // Reset input fields after adding product
                    productName = ""
                    productPrice = ""
                    imageResId = null

                    navController.navigate(Destinations.Rides.route)
                } else {
                    Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Product")
        }
    }
}


