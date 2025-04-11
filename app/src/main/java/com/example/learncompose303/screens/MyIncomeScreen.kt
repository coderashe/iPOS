package com.example.learncompose303.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
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
import com.example.learncompose303.ui.theme.Gray1
import com.example.learncompose303.ui.theme.MyBlueish
import com.example.learncompose303.viewmodels.InvoiceViewModel
import io.jetchart.bar.Bar
import io.jetchart.bar.BarChart
import io.jetchart.bar.Bars
import io.jetchart.bar.renderer.label.SimpleBarLabelDrawer
import io.jetchart.bar.renderer.label.SimpleBarValueDrawer
import io.jetchart.bar.renderer.xaxis.BarXAxisDrawer
import io.jetchart.bar.renderer.yaxis.BarYAxisWithValueDrawer
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.pie.PieChart
import io.jetchart.pie.Pies
import io.jetchart.pie.Slice
import io.jetchart.pie.renderer.FilledSliceDrawer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyIncomeScreen(
    viewModel: InvoiceViewModel = viewModel(),
    navController: NavController
) {
    var selectedFilter by remember { mutableStateOf("Day") }

    val salesData = when (selectedFilter) {
        "Day" -> viewModel.getTodaySalesInfo()
        "Week" -> viewModel.getWeeklySalesInfo()
        "Month" -> viewModel.getMonthlySalesInfo()
        else -> viewModel.getTodaySalesInfo()
    }

    val (currencyTotals, numberOfTransactions) = salesData as Pair<Map<String, Double>, Int>

    val periodDescription = when (selectedFilter) {
        "Day" -> "Today"
        "Week" -> {
            val today = LocalDate.now()
            val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            "${startOfWeek.format(DateTimeFormatter.ofPattern("MMM d"))} - ${
                endOfWeek.format(DateTimeFormatter.ofPattern("MMM d"))
            }"
        }

        "Month" -> LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        else -> "Today"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HighlightedButtonRow(selectedFilter) { filter ->
                    selectedFilter = filter
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = periodDescription,
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray1
                )

                Spacer(modifier = Modifier.height(24.dp))

                currencyTotals.forEach { (currency, amount) ->
                    Text(
                        text = "$currency ${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$numberOfTransactions transactions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray1
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pie Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Currency Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.DarkGray
                )

                SalesPieChart(currencyTotals)
            }
        }
    }
}

@Composable
fun SalesPieChart(currencyTotals: Map<String, Double>) {
    // Convert currency totals to pie slices
    val slices = currencyTotals.entries.mapIndexed { index, entry ->
        // Create a slice for each currency with proper colors
        val color = when (index % 4) {
            0 -> Color(0xFF4CAF50) // Green
            1 -> Color(0xFF2196F3) // Blue
            2 -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFE91E63) // Pink
        }

        Slice(entry.value.toFloat(), color)
    }

    // Create legend data for our pie chart
    val legendItems = currencyTotals.entries.mapIndexed { index, entry ->
        val color = when (index % 4) {
            0 -> Color(0xFF4CAF50)
            1 -> Color(0xFF2196F3)
            2 -> Color(0xFFFF9800)
            else -> Color(0xFFE91E63)
        }

        LegendItem(
            color = color,
            text = "${entry.key}: ${String.format("%.2f", entry.value)}"
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieChart(
            pies = Pies(slices),
            modifier = Modifier.height(340.dp),
            animation = fadeInAnimation(4000),
            sliceDrawer = FilledSliceDrawer(thickness = 60f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the legend
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            legendItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(item.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

data class LegendItem(val color: Color, val text: String)

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