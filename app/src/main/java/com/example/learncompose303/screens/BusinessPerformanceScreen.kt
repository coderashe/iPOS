package com.example.learncompose303.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import com.example.learncompose303.screens.performance.BusinessPerformanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun BusinessPerformanceScreen(viewModel: BusinessPerformanceViewModel = viewModel()) {
    val totalExpenses by viewModel.totalExpenses.observeAsState(0.0) // Ensure non-null value
    val totalSales by viewModel.totalSales.observeAsState(0.0)
    val topPerformingProducts by viewModel.topPerformingProducts.observeAsState(emptyList())
    val peakHours by viewModel.peakHours.observeAsState(emptyList())
    val salesPerMonth by viewModel.salesPerMonth.observeAsState(emptyMap())

    val dateFormat = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )     // SimpleDateFormat for hour display (HH:mm)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display Total Expenses and Sales
        Text(
            text = "Total Expenses: $${"%.2f".format(totalExpenses)}", // Ensure default if totalExpenses is null
        )
        Text(text = "Total Sales: $${"%.2f".format(totalSales)}")

        Spacer(modifier = Modifier.height(16.dp))

        // Display Top Performing Products
        Text(text = "Top Performing Products", style = MaterialTheme.typography.headlineSmall)
        topPerformingProducts.forEach { product ->
            Text(
                text = "${product.productName}: ${product.totalSold} units, Revenue: $${
                    "%.2f".format(
                        product.totalRevenue
                    )
                }"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Peak Sales Hours
        Text(text = "Peak Sales Hours", style = MaterialTheme.typography.headlineSmall)
        peakHours.forEach { hourPerformance ->
            // Format the 'hour' field as 'HH:mm' using the SimpleDateFormat
            val formattedHour = dateFormat.format(Date(hourPerformance.hour * 1000))
            Text(
                text = "Hour $formattedHour: Total Sales: $${"%.2f".format(hourPerformance.totalSales)}"
            )

        }

        Spacer(modifier = Modifier.height(16.dp))

//        // Display Sales Per Month
//        Text(text = "Sales Per Month", style = MaterialTheme.typography.headlineSmall)
//        salesPerMonth.forEach { (month, sales) ->
//            Text(text = "$month: $${"%.2f".format(sales)}")
//        }

        // Display Sales Per Month
        Text(text = "Sales Per Month", style = MaterialTheme.typography.headlineSmall)
        salesPerMonth.forEach { (month, sales) ->
            Text(text = "$month: $${"%.2f".format(sales)}")
        }

    }
}
