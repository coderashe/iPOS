package com.example.learncompose303.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.R
import com.example.learncompose303.app.DisplayArrowLeft
import com.example.learncompose303.app.DisplayArrowRight
import com.example.learncompose303.app.DisplayCurrentDate
import com.example.learncompose303.app.DisplayTotalBalance
import com.example.learncompose303.app.ThreeItemRow
import com.example.learncompose303.app.TwoItemColumn
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyGray
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.viewmodels.InvoiceViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyIncomeScreen(
    viewModel: InvoiceViewModel = viewModel(),
    navController: NavController
) {
    // State to track the selected filter (Day, Week, Month)
    var selectedFilter by remember { mutableStateOf("Day") }

    // Variable to hold the current sales info based on filter
    val (currentSalesTotal, numberOfTransactions) = when (selectedFilter) {
        "Day" -> viewModel.getTodaySalesInfo()
        "Week" -> viewModel.getWeeklySalesInfo()
        "Month" -> viewModel.getMonthlySalesInfo()
        else -> viewModel.getTodaySalesInfo()
    }

    Column(
        modifier = Modifier
            .background(MyGray)
            .verticalScroll(rememberScrollState())  // Makes it scrollable if content is large
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1st layer filter by day, week, month
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
            ) {
                HighlightedButtonRow(selectedFilter) { filter ->
                    selectedFilter = filter  // Update the filter state when a button is pressed
                }
            }
            // Display current date
            DisplayCurrentDate()

            // ArrowAndBalanceRow will display the balance based on the selected filter
            ArrowAndBalanceRow(currentSalesTotal.toString())

            // No complete orders section
            CompleteOrdersSection()
        }

        Spacer(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
                .background(Color.Gray)
        )

        // Last section :: history, achievements
        HistorySection(onClick = {
            navController.navigate(Destinations.Reports.route)
        })
    }
}


@Composable
fun HistorySection(onClick: () -> Unit) {
    ThreeItemRow(
        modifier = Modifier
            .padding(12.dp)
            .clickable(onClick = onClick),
        startContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "",
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between the icon and text
                Text(
                    text = stringResource(R.string.goto_request_history),
                    color = Color.Black
                )
            }
        },
        centerContent = {
            // Empty composable since centerContent will be empty in this case
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun AchievementsSection() {
    ThreeItemRow(
        modifier = Modifier.padding(12.dp),
        startContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "",
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between the icon and text
                Text(
                    text = stringResource(R.string.achievements),
                    color = Color.Black
                )
            }
        },
        centerContent = {
            // Empty composable since centerContent will be empty in this case
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun ArrowAndBalanceRow(balance: String?) {
    ThreeItemRow(
        startContent = {
            DisplayArrowLeft(modifier = Modifier)
        },
        centerContent = {
            DisplayTotalBalance(modifier = Modifier, "$${balance}" ?: "$0.0")
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun CompleteOrdersSection() {
    TwoItemColumn(
        modifier = Modifier.height(260.dp),
        topContent = {
            Image(
                painter = painterResource(id = R.drawable.istartlogo),
                contentDescription = "istartk"
            )
        },
        bottomContent = {
            Text(
                text = stringResource(R.string.no_complete_orders),
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    )
}

@Composable
fun HighlightedButtonRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
    ) {
        // First Button (Day)
        Button(
            onClick = { onFilterSelected("Day") },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedFilter == "Day") Color(0xFF3B5998) else Color.LightGray,
                contentColor = if (selectedFilter == "Day") Color.White else Color.Black
            )
        ) {
            Text(
                text = "Day",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Second Button (Week)
        Button(
            onClick = { onFilterSelected("Week") },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedFilter == "Week") Color(0xFF3B5998) else Color.LightGray,
                contentColor = if (selectedFilter == "Week") Color.White else Color.Black
            )
        ) {
            Text(
                text = "Week",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Third Button (Month)
        Button(
            onClick = { onFilterSelected("Month") },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedFilter == "Month") Color(0xFF3B5998) else Color.LightGray,
                contentColor = if (selectedFilter == "Month") Color.White else Color.Black
            )
        ) {
            Text(
                text = "Month",
                fontWeight = FontWeight.Bold
            )
        }
    }
}