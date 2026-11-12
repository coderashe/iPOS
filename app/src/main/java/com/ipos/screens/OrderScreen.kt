package com.ipos.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun OrderScreen(
    onSubmit: (String, Int, String, String) -> Unit // Callback for handling form submission
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Calendar and Context
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Function to show the DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dueDate = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Product Name Input
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quantity Input
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Due Date Input (with Calendar Picker)
        OutlinedTextField(
            modifier = Modifier
                .clickable { datePickerDialog.show() }
                .fillMaxWidth(),
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date") },
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

        Spacer(modifier = Modifier.height(8.dp))

        // Additional Notes Input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                // Pass the input values back via callback
                if (productName.isNotEmpty() && quantity.isNotEmpty() && dueDate.isNotEmpty()) {
                    onSubmit(productName, quantity.toInt(), dueDate, notes)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Submit")
        }
    }
}


@Preview
@Composable
fun PreviewOrderScreen() {
    OrderScreen(onSubmit = { productName, quantity, dueDate, notes ->
        // For preview purposes, we can log the inputs or leave this as a placeholder
        println("Product: $productName, Quantity: $quantity, DueDate: $dueDate, Notes: $notes")
    })
}
