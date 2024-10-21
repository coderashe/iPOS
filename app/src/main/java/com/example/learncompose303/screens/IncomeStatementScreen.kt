package com.example.learncompose303.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import com.example.learncompose303.ui.theme.Green1
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.LightGreen
import com.example.learncompose303.ui.theme.MyBlueish
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomeStatementScreen(
    viewModel: BusinessPerformanceViewModel = viewModel()
) {
    // Observe the states from the ViewModel
    val totalSalesState = viewModel.totalSales.observeAsState(0.0) // Observe total sales
    val totalExpensesState = viewModel.totalExpenses.observeAsState(0.0) // Observe total expenses

    // Safely handle potential null values by using default 0.0
    val totalSales = totalSalesState.value
    val totalExpenses = totalExpensesState.value

    // Calculate Profit/Loss
    val profitOrLoss = totalSales - totalExpenses

    // Display the Income Statement UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Income Statement for the Current Month",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Revenue (Total Sales)
        Text(
            text = "Revenue (Total Sales)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Sales", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$${"%.2f".format(totalSales)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Divider(color = Color.Gray)

        // Expenses Section
        Text(
            text = "Expenses (Direct + Indirect Expenses)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Total Expenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Expenses", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$${"%.2f".format(totalExpenses)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        // Profit or Loss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (profitOrLoss >= 0) "Profit" else "Loss",
                style = MaterialTheme.typography.titleLarge,
                color = if (profitOrLoss >= 0) MaterialTheme.colorScheme.primary else Color.Red
            )
            Text(
                text = "$${"%.2f".format(profitOrLoss)}",
                style = MaterialTheme.typography.titleLarge,
                color = if (profitOrLoss >= 0) MaterialTheme.colorScheme.primary else Color.Red
            )
        }

    }
}
