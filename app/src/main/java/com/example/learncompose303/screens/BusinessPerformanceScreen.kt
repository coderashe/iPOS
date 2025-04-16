package com.example.learncompose303.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.More
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.viewmodels.InvoiceViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.learncompose303.R
import com.example.learncompose303.app.utils.exportDatabase
import com.example.learncompose303.data.AppDatabase
import com.example.learncompose303.screens.performance.BusinessPerformanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BusinessPerformanceScreen(
    viewModel: BusinessPerformanceViewModel = viewModel(),
    navController: NavController
) {
    // Observe the states, and assign them to variables
    val totalExpensesState = viewModel.totalExpenses.observeAsState(null)
//    val totalSalesState = viewModel.totalSales.observeAsState(null)
    val totalSalesState = viewModel.totalSalesInUsd.observeAsState(null)

    val totalSales = totalSalesState.value ?: 0.0
    // Safely handle potential null values by using Elvis operator here
    val totalExpenses = totalExpensesState.value ?: 0.0

    val topPerformingProducts by viewModel.topPerformingProducts.observeAsState(emptyList())
    val peakHours by viewModel.peakHours.observeAsState(emptyList())
    val salesPerMonth by viewModel.salesPerMonth.observeAsState(emptyMap())

    // Calculate profitLoss safely
    val profitLoss = (totalSales - totalExpenses)

    val dateFormat = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )     // SimpleDateFormat for hour display (HH:mm)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            color = Color.Black,
            text = stringResource(R.string.business_position),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "INCOME STATEMENT",
            style = MaterialTheme.typography.headlineSmall,
        )

        Row {
            // Revenue (Total Sales)
            Text(
                text = stringResource(R.string.total_sales_in_usd),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

        }

        Row {

            Text(
                modifier = Modifier.weight(1f),
                color = Color.Black,
                text = "Total Sales ",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                color = Color.Black,
                text = "$${"%.2f".format(totalSales)}",
                style = MaterialTheme.typography.titleSmall
            )
        }
        Divider(color = Color.Gray)

        Row {
            // Expenses Section
            Text(
                text = "Expenses (Direct + Indirect Expenses)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row {
            Text(
                modifier = Modifier.weight(1f),
                color = Color.Black,
                text = "Total Expenses ",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                color = Color.Black,
                text = "$${"%.2f".format(totalExpenses)}",
                style = MaterialTheme.typography.titleSmall
            )
        }
        Divider(color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text(
                modifier = Modifier.weight(1f),
                color = if (profitLoss >= 0) MaterialTheme.colorScheme.primary else Color.Red,
                text = "Profit / Loss ",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                color = if (profitLoss >= 0) MaterialTheme.colorScheme.primary else Color.Red,
                text = "$${"%.2f".format(profitLoss)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Display Top Performing Products
        Text(
            color = Color.Black,
            text = stringResource(R.string.top_performing_products),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(2.dp))
        topPerformingProducts.forEach { product ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), // Add some vertical padding between rows
                horizontalArrangement = Arrangement.SpaceBetween // Spread out items evenly
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    text = product.productName, // add productCode
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    modifier = Modifier.weight(0.5f),
                    color = Color.Black,
                    text = "Sold: ${product.totalSold} units",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.weight(0.5f),
                    color = Color.Black,
                    text = "Left: ${product.currentStock.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    text = "Total Sales: $${"%.2f".format(product.totalRevenue)}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End
                )
            }
            Divider(color = Color.Gray)
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Display Peak Sales Hours
        Text(
            color = Color.Black,
            text = stringResource(R.string.peak_sales_hour),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(2.dp)) // Add some space before the list

        peakHours.forEach { hourPerformance ->
            // Format the 'hour' field as 'HH:mm' using the SimpleDateFormat
            val formattedHour = dateFormat.format(Date(hourPerformance.hour * 1000))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp), // Add vertical padding between rows
                horizontalArrangement = Arrangement.SpaceBetween // Spread out items evenly
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    text = "Hour $formattedHour",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    text = "Total Sales: $${"%.2f".format(hourPerformance.totalSales)}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End
                )
            }
            Divider(color = Color.Gray) // Optional: To separate each hour's performance visually
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Sales Per Month
        Text(
            color = Color.Black,
            text = "TOTAL in USD FOR THE MONTH OF:",
            style = MaterialTheme.typography.headlineSmall
        )
        salesPerMonth.forEach { (month, sales) ->
            Text(
                color = Color.Black,
                text = "$month:   $${"%.2f".format(sales)}"
            )
        }
        val context = LocalContext.current

        Button(
            onClick = {
                exportDatabase(context, AppDatabase.DATABASE_NAME)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Export Database")
        }

    }
}
