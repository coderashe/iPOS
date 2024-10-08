package com.example.learncompose303.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.viewmodels.InvoiceViewModel


@Composable
fun LoadStockScreen(
    viewModel: InvoiceViewModel,
    navController: NavController,
    onProductAdded: () -> Unit // Callback when a product is added
) {
    var productName by remember { mutableStateOf("") }
    var currentStockLevel by remember { mutableStateOf("") }
    var unitCost by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    val context = LocalContext.current

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

        // Current Stock Level Input
        OutlinedTextField(
            value = currentStockLevel,
            onValueChange = { currentStockLevel = it },
            label = { Text("Current Stock Level") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Unit Cost Input
        OutlinedTextField(
            value = unitCost,
            onValueChange = { unitCost = it },
            label = { Text("Unit Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selling Price Input
        OutlinedTextField(
            value = sellingPrice,
            onValueChange = { sellingPrice = it },
            label = { Text("Selling Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to Add Product and its Price
        Button(
            onClick = {
                val unitCostValue = unitCost.toDoubleOrNull()
                val sellingPriceValue = sellingPrice.toDoubleOrNull()
                val currentStockLevelValue = currentStockLevel.toDoubleOrNull()

                // Ensure valid inputs before performing calculations
                if (unitCostValue != null && currentStockLevelValue != null) {
                    val totalCost = unitCostValue * currentStockLevelValue

                    // Calculate gross profit percentage and markup percentage
                    val grossProfitPercentage = if (sellingPriceValue != null) {
                        viewModel.calculateGrossProfitPercentage(unitCostValue, sellingPriceValue)
                    } else {
                        0.0 // Default to 0 if selling price is not valid
                    }

                    val markupPercentage = if (sellingPriceValue != null) {
                        viewModel.calculateMarkupPercentage(unitCostValue, sellingPriceValue)
                    } else {
                        0.0 // Default to 0 if selling price is not valid
                    }


                    // Calculate standard cost (e.g., as a weighted cost or an average cost)
                    val standardCost =
                        viewModel.calculateStandardCost(unitCostValue, currentStockLevelValue)

                    // Create and insert Product
                    val newProduct = Product(
                        productDescription = productName,
                        currentStockLevel = currentStockLevel.toDoubleOrNull() ?: 0.0,
                        saleable = true,
                        purchasable = true,
                        averageCost = totalCost,
                        standardCost = standardCost,
                        salesTaxCode = "VAT",
                        purchasableTaxCode = "VAT",
                        inclusiveTaxed = true,
                    )

                    viewModel.insertTheProduct(newProduct) { productId ->
                        // Create and insert PriceList entry
                        val newPriceList = sellingPriceValue?.let {
                            PriceList(
                                productId = productId,
                                productDescription = productName,
                                unitCost = unitCostValue,
                                sellingPrice = it,
                                markupPercentage = markupPercentage,
//                                viewModel.calculateMarkupPercentage(
//                                    unitCostValue,
//                                    sellingPriceValue
//                                ),
                                grossProfitPercentage = grossProfitPercentage
//                                viewModel.calculateGrossProfitPercentage(
//                                    unitCostValue, sellingPriceValue
//                                )
                            )
                        }
                        if (newPriceList != null) {
                            viewModel.insertPriceList(newPriceList)
                        }

                        onProductAdded()
                        Toast.makeText(context, "Product and Price added!", Toast.LENGTH_SHORT)
                            .show()

                        // Reset input fields
                        productName = ""
                        unitCost = ""
                        currentStockLevel = ""
                        sellingPrice = ""

                        navController.navigate(Destinations.Rides.route)
                    }

                } else {
                    Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        ) {
            Text("Add Product")
        }
    }
}
