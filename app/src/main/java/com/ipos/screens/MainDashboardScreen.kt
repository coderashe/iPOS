package com.ipos.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.InsertDriveFile
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipos.R
import com.ipos.data.Destinations
import com.ipos.viewmodels.InvoiceViewModel

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
fun QuickAccessButtons(
    navController: NavController,
    viewModel: InvoiceViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val context = LocalContext.current
        val requestFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.let { inputStream ->
                        viewModel.processCsvInvoices(inputStream, "dr.simbastart@gmail.com")
                        Toast.makeText(context, "Processing invoice CSV...", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("BatchInvoice", "Error reading CSV file", e)
                    Toast.makeText(context, "Error reading CSV file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        QuickAccessCard(
            label = stringResource(R.string.invoicing),
            subTitle = "---",
            icon = Icons.Default.Receipt,
            onClick = {
                navController.navigate(Destinations.ProductSelection.route)
            }
        )
        QuickAccessCard(
            label = stringResource(R.string.reports),
            subTitle = "---",
            icon = Icons.Default.BarChart,
            onClick = {
                navController.navigate(Destinations.Reports.route)  /* Navigate to Reports Screen */
            }
        )
        QuickAccessCard(
            label = stringResource(R.string.load_stocks),
            subTitle = "---",
            icon = Icons.Default.AddShoppingCart,
            onClick = {
                navController.navigate(Destinations.LoadStocks.route) // Navigate to Stock Loading Screen
            }
        )
        QuickAccessCard(
            label = stringResource(R.string.orders),
            subTitle = "---",
            icon = Icons.Default.More,
            onClick = { navController.navigate(Destinations.OrderReports.route)             /* Navigate to OrderReports Screen */ }
        )
        QuickAccessCard(
            label = stringResource(R.string.stockTaking),
            subTitle = "---",
            icon = Icons.Default.AttachMoney,
            onClick = { navController.navigate(Destinations.StockTaking.route)             /* Navigate to OrderReports Screen */ }
        )
        QuickAccessCard(
            label = stringResource(R.string.bstchInvoicing),
            subTitle = "---",
            icon = Icons.Default.InsertDriveFile,
            onClick = { requestFileLauncher.launch("*/*") }
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

            // Text Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp)) // Space between label and subtitle

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SaleCard(
    modifier: Modifier = Modifier.padding(8.dp),
    label: String,
    subTitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp)) // Space between icon and text

            // Text Content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp)) // Space between label and subtitle

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
