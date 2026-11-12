package com.ipos.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipos.screens.expenses.ExpenseViewModel


@Composable
fun ExpensesScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = viewModel()
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Set the selected date in expenseDate
                expenseDate = "$dayOfMonth/${month + 1}/$year"
                // Update calendar instance with selected date
                calendar.set(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Expense Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Input
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Expense Date Input (with Calendar Picker)
        OutlinedTextField(
            modifier = Modifier
                .clickable { datePickerDialog.show() }
                .fillMaxWidth(),
            value = expenseDate,
            onValueChange = { expenseDate = it },
            label = { Text("Expense Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick Date"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                // Check if inputs are valid
                if (description.isNotEmpty() && amount.isNotEmpty() && category.isNotEmpty() && expenseDate.isNotEmpty()) {
                    // Parse the amount and prepare the expense date
                    val parsedAmount = amount.toDoubleOrNull()
                    val expenseDateLong = calendar.timeInMillis

                    // Add the expense if the amount is valid
                    if (parsedAmount != null) {
                        viewModel.addExpense(
                            description = description,
                            amount = parsedAmount,
                            expenseDate = expenseDateLong,
                            category = category
                        )

                        // Optionally navigate back or show a success message
                        navController.popBackStack()
                    } else {
                        // Handle invalid amount input (e.g., show a Toast message)
                        Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle missing inputs (e.g., show a Toast message)
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add Expense")
        }
    }
}


