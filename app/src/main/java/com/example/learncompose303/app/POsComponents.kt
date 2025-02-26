package com.example.learncompose303.app

import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.DatePickerDialog
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.screens.ShopLandingScreen
import com.example.learncompose303.ui.theme.Gray1
import com.example.learncompose303.ui.theme.Green1
import com.example.learncompose303.viewmodels.InvoiceViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.Calendar
import coil.compose.rememberAsyncImagePainter
import coil.size.Size
import coil.request.ImageRequest
import kotlinx.coroutines.coroutineScope
import coil.compose.AsyncImage
import coil.size.Scale
import com.example.learncompose303.ui.theme.MediumGreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeRefreshLayout() {
    // State to manage the refresh status
    var isRefreshing by remember { mutableStateOf(false) }

    // SwipeRefresh composable from Accompanist
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            // Perform refresh logic here
            isRefreshing = true

            // Simulate loading and recomposition
            // You can add your actual refresh logic here (like fetching new data)
            Handler(Looper.getMainLooper()).postDelayed({
                isRefreshing = false  // Stop refreshing after some time
            }, 1000)
        }
    ) {
        // The content to be refreshed
        ShopLandingScreen()
    }
}

//@Composable
//fun AutoSlidingCarousel(
//    modifier: Modifier = Modifier,
//    navController: NavController,
//    viewModel: InvoiceViewModel = viewModel()
//) {
//    val products by viewModel.productList.collectAsState(initial = emptyList())
//    val productDescriptions = products.take(3).map { it.productDescription }
//    val productImages = products.take(3).map { it.imageUri } // Get image URIs as Strings
//    val pageCount = if (productDescriptions.isNotEmpty()) productDescriptions.size else 1
//    val coroutineScope = rememberCoroutineScope()
//    val pagerState = rememberPagerState(pageCount = { pageCount })
//
//    if (pageCount > 1) {
//        LaunchedEffect(pagerState) {
//            while (true) {
//                yield()
//                delay(5000L)
//                val nextPage = (pagerState.currentPage + 1) % pageCount
//                pagerState.animateScrollToPage(
//                    page = nextPage,
//                    animationSpec = tween(
//                        durationMillis = 600,
//                        easing = FastOutSlowInEasing
//                    )
//                )
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier.fillMaxWidth()
//        ) { page ->
//            if (productDescriptions.isNotEmpty()) {
//                CarouselCard(
//                    headingText = productDescriptions[page],
//                    taglineText = "Explore our amazing products.",
//                    buttonText = "View Product",
//                    imageUrl = productImages[page], // Pass String? URI here
//                    onButtonClick = {
//                        navController.navigate(Destinations.CustomerSelection.route)
//                    }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            repeat(pageCount) { index ->
//                Box(
//                    modifier = Modifier
//                        .size(10.dp)
//                        .background(
//                            if (pagerState.currentPage == index) Green1 else Color.Gray,
//                            shape = RoundedCornerShape(50)
//                        )
//                        .padding(4.dp)
//                        .clickable {
//                            coroutineScope.launch {
//                                pagerState.scrollToPage(index)
//                            }
//                        }
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//            }
//        }
//    }
//}


//working working but large image loading error
@Composable
fun CarouselCard(
    headingText: String,
    taglineText: String,
    buttonText: String,
    imageUrl: String?,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
//        elevation = 8.dp, Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column for text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = headingText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = taglineText,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Text(text = buttonText)
                }
            }

            // Column for image content
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    placeholder = painterResource(R.drawable.bootsu), // Add a placeholder image
                    error = painterResource(R.drawable.bootsu) // Add an error fallback image
                ),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: InvoiceViewModel = viewModel()
) {
    // Collect products from the ViewModel using collectAsState
    val products by viewModel.productList.collectAsState(initial = emptyList())
    // Get the first 3 product descriptions from the product list
    val productDescriptions = products.take(3).map { it.productDescription }
    val productImages = products.take(3).map { it.imageUri } // Get image URIs as Strings
    // Avoid creating a pager with zero pages
    val pageCount = if (productDescriptions.isNotEmpty()) productDescriptions.size else 1
    val coroutineScope = rememberCoroutineScope() // Coroutine scope to launch coroutines
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Only enable auto-slide if there are more than 1 pages
    if (pageCount > 1) {
        // Auto-slide functionality with LaunchedEffect and coroutine delay
        LaunchedEffect(pagerState) {
            while (true) {
                yield() // Yield ensures smooth continuation
                delay(5000L) // Delay of 5 seconds for auto slide
                val nextPage = (pagerState.currentPage + 1) % pageCount
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(
                        durationMillis = 600, // Adjust the duration for smoother animation
                        easing = FastOutSlowInEasing // Easing function for smooth acceleration/deceleration
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            // Dynamically display the product description in the carousel card
            if (productDescriptions.isNotEmpty()) {
                CarouselCard(
                    headingText = productDescriptions[page], // Use product description as heading
                    taglineText = "Explore our amazing products.",
                    buttonText = "View Product",
                    imageUrl = productImages[page],
                    onButtonClick = {
                        navController.navigate(Destinations.CustomerSelection.route) // Navigate to a screen
                    }
                )
            }
        }

        // Indicator dots below the carousel
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (pagerState.currentPage == index) Green1 else Color.Gray,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(4.dp)
                        .clickable {
                            // Use coroutineScope to launch the scrollToPage call
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesmanInfoCard(
    salesmanName: String,
    shopName: String,
    viewModel: InvoiceViewModel = viewModel()
) {
    val invoiceList by viewModel.invoiceList.collectAsState()
    val (oldSalesTotal, numberOfTransactions) = viewModel.getTodaySalesInfo()

    // Group invoices by currency and sum totals
    val currencyTotals = invoiceList.groupBy { it.currencyCode }.mapValues { entry ->
        entry.value.sumOf { it.totalAmount }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name of Salesman
            Text(
                text = salesmanName,
                style = MaterialTheme.typography.titleMedium,
            )

            // The Shop
            Text(
                text = "@$shopName",
                style = MaterialTheme.typography.bodySmall,
                color = Gray1
            )
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Display Sales Totals by Currency
                Column {
                    Text(
                        text = "SALES TOTALS",
                        style = MaterialTheme.typography.titleSmall
                    )

                    currencyTotals.forEach { (currencyCode, totalAmount) ->
                        Text(
                            text = "$currencyCode: ${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGreen
                        )
                    }
                }

                // Number of transactions
                Column {
                    Text(
                        text = "no.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "$numberOfTransactions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MediumGreen
                    )
                }
            }
        }
    }
}

@Composable
fun SpacerHeight() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DueDatePicker(dueDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // State to track the selected due date
    var selectedDate by remember { mutableStateOf(dueDate) }

    // Date Picker Dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
                onDateSelected(selectedDate) // Notify parent about the selected date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Due Date Input (with Calendar Picker)
    OutlinedTextField(
        value = selectedDate,
        onValueChange = { }, // No manual changes allowed for read-only input
        label = { Text("Due Date") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }, // Show the date picker dialog on click
        readOnly = true, // Prevent typing, make it clickable only
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Pick Date"
            )
        }
    )
}

@Composable
fun Title() {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.onBackground,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onPrimary.copy(0.5f)
    )
    var offsetX by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = tween(3000)
        ) { value, _ ->
            offsetX = value
        }
    }
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(offsetX, 0f),
        end = Offset(offsetX + 200f, 100f)
    )
    Text(
        stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.titleLarge.copy(brush = brush)
    )
}