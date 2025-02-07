package com.example.learncompose303.screens

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Calendar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Expense
import com.example.learncompose303.screens.expenses.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseReportScreen(
    viewModel: ExpenseViewModel = viewModel(),
    navController: NavController,
) {
    // Collect expenses and total expenses from the ViewModel
    val expenses by viewModel.allExpenses.observeAsState(emptyList())
    val totalExpenses by viewModel.totalExpenses.observeAsState(0.0) // Ensure non-null value

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display total expenses balance at the top
            Text(
                text = "Total Expenses: $${"%.2f".format(totalExpenses)}", // Ensure default if totalExpenses is null
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Check if there are any expenses to display
            if (expenses.isEmpty()) {
                Text(
                    text = "No Expenses Found",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseCard(expense = expense)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate(Destinations.Expense.route)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense"
            )
        }
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Description
            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyMedium
            )

            // Amount
            Text(
                text = "Amount: $${"%.2f".format(expense.amount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Category
            Text(
                text = "Category: ${expense.category}",
                style = MaterialTheme.typography.bodySmall
            )

            // Expense Date
            val expenseDate = Date(expense.expenseDate)
            Text(
                text = "Date: ${
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                        expenseDate
                    )
                }",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

