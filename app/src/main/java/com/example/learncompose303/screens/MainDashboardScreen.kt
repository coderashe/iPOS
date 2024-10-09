package com.example.learncompose303.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations

@Composable
fun MainDashboardScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Greeting or App Title
        Text(
            text = "Welcome to iPOS",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Stock Overview Section
        StockOverviewSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Sales Section
        RecentSalesSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Access Buttons
//        QuickAccessButtons()
    }
}

@Composable
fun StockOverviewSection() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Stock Levels",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Available: 120 items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Low Stock: 5 items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecentSalesSection() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Recent Sales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Example list of recent sales
            val sales = listOf(
                "Sale #123 - $50",
                "Sale #124 - $30",
                "Sale #125 - $40"
            )

            sales.forEach { sale ->
                Text(
                    text = sale,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun QuickAccessButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
    ) {
        QuickAccessCard(
            label = stringResource(R.string.invoicing),
            subTitle = "--",
            icon = Icons.Default.Receipt,
            onClick = {
                navController.navigate(Destinations.CustomerSelection.route) // Navigate to InvoiceScreen
            }
        )
        QuickAccessCard(
            label = stringResource(R.string.orders),
            subTitle = "--",
            icon = Icons.Default.More,
            onClick = { navController.navigate(Destinations.OrderReports.route)             /* Navigate to OrderReports Screen */ }
        )
        QuickAccessCard(
            label = stringResource(R.string.reports),
            subTitle = "--",
            icon = Icons.Default.BarChart,
            onClick = {
                navController.navigate(Destinations.Reports.route)  /* Navigate to Reports Screen */
            }
        )
        QuickAccessCard(
            label = stringResource(R.string.load_stocks),
            subTitle = "--",
            icon = Icons.Default.AddShoppingCart,
            onClick = {
                navController.navigate(Destinations.LoadStocks.route) // Navigate to Stock Loading Screen
            }
        )

    }
}

@Composable
fun QuickAccessCard(
    modifier: Modifier = Modifier.padding(8.dp),
    label: String,
    subTitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .width(142.dp)
            .padding(start = 4.dp)
            .background(Color.Transparent, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick) // Make the card clickable,
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start // Aligning contents to the start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Start),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = label, fontSize = 15.sp)
                Text(text = subTitle)
            }

        }
    }
}

@Composable
fun QuickAccessButton(
    modifier: Modifier = Modifier.padding(4.dp),
    label: String,
    subTitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(120.dp)
            .width(120.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subTitle)
            Text(text = label)
        }
    }
}

@Preview
@Composable
fun DashboardPrev() {
    val navController = rememberNavController()
    MainDashboardScreen(navController)
}
